package me.hwei.bukkit.scoreplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.PersistenceException;

import me.hwei.bukkit.scoreplugin.ScorePermissionManager.ScorePermissionType;
import me.hwei.bukkit.scoreplugin.data.Score;
import me.hwei.bukkit.scoreplugin.data.ScoreAggregate;
import me.hwei.bukkit.scoreplugin.data.Work;

import org.bukkit.ChatColor;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;



public class ScorePlugin extends JavaPlugin implements Listener, EventExecutor, CommandExecutor
{	
	protected ScoreConfiguation configuation = null;
	protected HashMap<String, IScoreSignOperation> playerSignOperation = new HashMap<String, IScoreSignOperation>();
	protected ScoreOutput output = null;
	protected ScoreSignOperationFactory signOperationFactory = null;
	protected ScorePermissionManager permissionManager = null;

	@Override
	public void onDisable() {
		for(Player player : this.getServer().getOnlinePlayers()) {
			this.permissionManager.RemoveAdmin(player);
		}
		this.output.ToConsole("Disabled.");
	}

	@Override
	public void onEnable() {
		this.configuation = new ScoreConfiguation(this.getConfiguration());
		this.output = new ScoreOutput(this.getServer().getLogger(), this.getDescription().getName(), this.getServer());
		this.setupDatabase();
		this.permissionManager = new ScorePermissionManager(this.getDescription().getPermissions(), this.configuation.getAdminList(), this);
		ScoreMoneyManager moneyManager = new ScoreMoneyManager(this.getServer().getPluginManager(), this.output);
		this.signOperationFactory = new ScoreSignOperationFactory(output, this.getDatabase(), this.permissionManager, this.configuation, moneyManager);

		this.getCommand("score").setExecutor(this);
		
		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvent(Event.Type.PLAYER_INTERACT, this, this, Priority.Normal, this);
		pluginManager.registerEvent(Event.Type.BLOCK_BREAK, this, this, Priority.Normal, this);
		pluginManager.registerEvent(Event.Type.PLAYER_JOIN, permissionManager, Priority.Normal, this);
		pluginManager.registerEvent(Event.Type.PLAYER_QUIT, permissionManager, Priority.Normal, this);
		pluginManager.registerEvent(Event.Type.PLAYER_KICK, permissionManager, Priority.Normal, this);
		pluginManager.registerEvent(Event.Type.PLUGIN_ENABLE, moneyManager, Priority.Monitor, this);
		pluginManager.registerEvent(Event.Type.PLUGIN_DISABLE, moneyManager, Priority.Monitor, this);
		     
        this.output.ToConsole("Enabled. Developed by " + this.getDescription().getAuthors().get(0) + ".");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0) {
			this.output.ToCommandSender(sender, command.getDescription());
			return true;
		}
		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("reload")) {
				if(!this.permissionManager.HasPermission(sender, ScorePermissionType.ADMIN)) {
					this.output.ToCommandSender(sender, "Do not have permissions.");
					return true;
				}
				this.configuation.Load();
				this.output.ToCommandSender(sender, "Config reloaded.");
				return true;
			}
			if(args[0].equalsIgnoreCase("listadmin")) {
				if(!this.permissionManager.HasPermission(sender, ScorePermissionType.ADMIN)) {
					this.output.ToCommandSender(sender, "Do not have permissions.");
					return true;
				}
				StringBuilder sb = new StringBuilder();
				for(String name : this.configuation.getAdminList()) {
					sb.append(name);
					sb.append(' ');
				}
				this.output.ToCommandSender(sender, sb.toString());
				return true;
			}
		}
		if(args.length == 2) {
			if(args[0].equalsIgnoreCase("addadmin")) {
				if(!this.permissionManager.HasPermission(sender, ScorePermissionType.ADMIN)) {
					this.output.ToCommandSender(sender, "Do not have permissions.");
					return true;
				}
				Player newAdmin = this.getServer().getPlayer(args[1]);
				if(newAdmin == null) {
					this.output.ToCommandSender(sender, "Can not find player " + ChatColor.GREEN + args[1] + ChatColor.WHITE + ".");
					return true;
				} else {
					this.permissionManager.AddAdmin(newAdmin);
					this.configuation.SaveAdminList();
					this.output.ToCommandSender(sender, "Added " + ChatColor.GREEN + args[1] + ChatColor.WHITE + " to scores admin.");
					return true;
				}
			}
			if(args[0].equalsIgnoreCase("removeadmin")) {
				if(!this.permissionManager.HasPermission(sender, ScorePermissionType.ADMIN)) {
					this.output.ToCommandSender(sender, "Do not have permissions.");
					return true;
				}
				Player oldAdmin = this.getServer().getPlayer(args[1]);
				if(oldAdmin == null) {
					this.output.ToCommandSender(sender, "Can not find player " + ChatColor.GREEN + args[1] + ChatColor.WHITE + ".");
					return true;
				} else {
					this.permissionManager.RemoveAdmin(oldAdmin);
					this.configuation.SaveAdminList();
					this.output.ToCommandSender(sender, "Removed " + ChatColor.GREEN + args[1] + ChatColor.WHITE + " from scores admin.");
					return true;
				}
			}
		}
		
		Player player = null;
		if(sender instanceof Player) {
			player = (Player)sender;
		}
		if(player == null)
			return false;
		
		IScoreSignOperation scoreCommand = this.signOperationFactory.Create(args, player);
		if(scoreCommand == null) {
			this.output.ToPlayer(player, "Don't have permissions or wrong command.");
			return false;
		}
		this.playerSignOperation.put(player.getName(), scoreCommand);
		this.output.ToPlayer(player, scoreCommand.GetHint());
		return true;
	}

	@Override
	public void execute(Listener listener, Event event) {
		if(event instanceof PlayerInteractEvent) {
			PlayerInteractEvent e = (PlayerInteractEvent)event;
			Player player = e.getPlayer();
			Block block = e.getClickedBlock();
			if(e.getAction() == Action.LEFT_CLICK_BLOCK && block.getState() instanceof Sign) {
				Sign sign = (Sign)block.getState();
				if(this.playerSignOperation.containsKey(player.getName())) {
					this.playerSignOperation.get(player.getName()).Execute(sign);
					this.playerSignOperation.remove(player.getName());
				} else {
					IScoreSignOperation signOperation = this.signOperationFactory.Create(new String[] {"info"}, player);
					if(signOperation != null)
						signOperation.Execute(sign);
				}
			}
			return;
		}
		if(event instanceof BlockBreakEvent) {
			BlockBreakEvent e = (BlockBreakEvent)event;
			Player player = e.getPlayer();
			if(player == null) {
				return;
			}
			if(e.getBlock().getState() instanceof Sign) {
				Sign sign = (Sign)e.getBlock().getState();
				if(this.playerSignOperation.containsKey(player.getName())) {
					this.playerSignOperation.get(player.getName()).Execute(sign);
					this.playerSignOperation.remove(player.getName());
				} else {
					IScoreSignOperation signOperation = this.signOperationFactory.Create(new String[] {"remove"}, player);
					if(signOperation == null) {
						Work work = this.getDatabase().find(Work.class)
						.where()
						.eq("pos_x", sign.getX())
						.eq("pos_y", sign.getY())
						.eq("pos_z", sign.getZ())
						.findUnique();
						if(work != null)
							e.setCancelled(true);
						return;
					} else {
						signOperation.Execute(sign);
						return;
					}
				}
			}
			return;
		}
	}
	
	protected void setupDatabase() {
		try {
			this.getDatabase().find(Work.class).findRowCount();
			this.getDatabase().find(Score.class).findRowCount();
		} catch (PersistenceException ex) {
			this.output.ToConsole("Installing database for " + this.getDescription().getName() + " due to first time usage");
			this.installDDL();
        }
	}
	
	@Override
	public List<Class<?>> getDatabaseClasses() {
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(Work.class);
        list.add(Score.class);
        list.add(ScoreAggregate.class);
        return list;
	}

}
