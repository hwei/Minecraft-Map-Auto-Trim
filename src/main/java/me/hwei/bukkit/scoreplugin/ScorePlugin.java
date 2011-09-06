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
import org.bukkit.Location;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
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
	protected ScoreMoneyManager moneyManager = null;

	@Override
	public void onDisable() {
		this.output.ToConsole("Disabled.");
	}

	@Override
	public void onEnable() {
		this.configuation = new ScoreConfiguation(this.getConfiguration());
		this.output = new ScoreOutput(this.getServer().getLogger(), this.getDescription().getName(), this.getServer());
		this.setupDatabase();
		ScorePermissionManager permissionManager = new ScorePermissionManager(this.getDescription().getPermissions());
		this.moneyManager = new ScoreMoneyManager(this.getServer().getPluginManager(), this.output);
		this.signOperationFactory = new ScoreSignOperationFactory(output, this.getDatabase(), permissionManager, this.configuation, this.moneyManager);

		this.getCommand("score").setExecutor(this);
		
		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvent(Event.Type.PLAYER_INTERACT, this, this, Priority.Normal, this);
		pluginManager.registerEvent(Event.Type.BLOCK_BREAK, this, this, Priority.Normal, this);
		pluginManager.registerEvent(Event.Type.PLUGIN_ENABLE, moneyManager, Priority.Monitor, this);
		pluginManager.registerEvent(Event.Type.PLUGIN_DISABLE, moneyManager, Priority.Monitor, this);
		     
        this.output.ToConsole("Enabled. Developed by " + this.getDescription().getAuthors().get(0) + ".");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		
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
			
		}
		
		if(args.length == 1 || args.length == 2) {
			if(args[0].equalsIgnoreCase("list")) {
				if(!this.permissionManager.HasPermission(sender, ScorePermissionType.USE)) {
					this.output.ToCommandSender(sender, "Do not have permissions.");
					return true;
				}
				int pageSize = 10;
				if(args.length == 2) {
					try {
						pageSize = Integer.parseInt(args[1]);
						if(pageSize <= 0) {
							pageSize = 10;
						}
					} catch(NumberFormatException e) {
					}
				}
				List<Work> recent_open_list = this.getDatabase()
						.find(Work.class)
						.where()
						.eq("reward", null)
						.orderBy("work_id desc")
						.setMaxRows(pageSize)
						.findList();
				for(int i=0; i<recent_open_list.size(); ++i) {
					this.output.ToCommandSender(sender, "" + (i + 1) + ". "
							+ ChatColor.GREEN + recent_open_list.get(i).getName() + ChatColor.WHITE
							+ " author: "
							+ ChatColor.DARK_GREEN + recent_open_list.get(i).getAuthor());
				}
				return true;
			}
		}
		
		if(args.length == 2) {
			if(args[0].equalsIgnoreCase("tp")) {
				if(!this.permissionManager.HasPermission(sender, ScorePermissionType.USE)) {
					this.output.ToCommandSender(sender, "Do not have permissions.");
					return true;
				}
				if(!(sender instanceof Player)) {
					this.output.ToCommandSender(sender, "Can not teleport you.");
					return true;
				}
				int tpId = -1;
				try {
					tpId = Integer.parseInt(args[1]);
					if(tpId <= 0) {
						return false;
					}
				} catch(NumberFormatException e) {
					return false;
				}
				Work work = this.getDatabase()
					.find(Work.class)
					.where()
					.eq("reward", null)
					.orderBy("work_id desc")
					.setFirstRow(tpId - 1)
					.setMaxRows(1)
					.findUnique();
				if(work == null) {
					this.output.ToCommandSender(sender, "Teleport id " + tpId + " does not exist.");
					return true;
				}
				player = (Player)sender;
				if(this.configuation.getTp_price() != 0.0) {
					if(!this.moneyManager.TakeMoney(player.getName(), this.configuation.getTp_price())) {
						this.output.ToPlayer(player, "You should have at least " + ChatColor.GREEN
								+ this.moneyManager.Format(this.configuation.getTp_price())
								+ ChatColor.WHITE + " for teleport." );
						return true;
					}
				}
				Location l = new Location(player.getWorld(), work.getPos_x(), work.getPos_y(), work.getPos_z());
				player.teleport(l);
				this.output.ToPlayer(player, "Teleporting to " + ChatColor.GREEN + work.getName() + ChatColor.WHITE + " ...");
				return true;
			}
		}
		
		
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
