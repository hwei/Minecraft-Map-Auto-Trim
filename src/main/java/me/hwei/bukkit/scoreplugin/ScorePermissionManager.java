package me.hwei.bukkit.scoreplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

public class ScorePermissionManager extends PlayerListener {
	public ScorePermissionManager(ArrayList<Permission> permissions, List<String> adminList, Plugin plugin) {
		for(Permission permission : permissions) {
			if(permission.getName().equalsIgnoreCase("score.use")) {
				this.permissionUse = permission;
				continue;
			}
			if(permission.getName().equalsIgnoreCase("score.admin")) {
				this.permissionAdmin = permission;
				continue;
			}
		}
		this.adminList = adminList;
		this.plugin = plugin;
		this.playerPermissions = new HashMap<String, PermissionAttachment>();
	}
	public boolean HasPermission(Permissible permissible, ScorePermissionType permissioinType) {
		switch (permissioinType) {
		case USE:
			return this.hasPermissionUse(permissible);
		case ADMIN:
			return this.hasPermissionAdmin(permissible);
		}
		return false;
	}
	public enum ScorePermissionType {    
        USE, ADMIN
    };
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
    	if(this.permissionAdmin == null)
			return;
    	Player player = event.getPlayer();
		String name = player.getName();
		if(this.adminList.contains(name)) {
			PermissionAttachment pa = player.addAttachment(this.plugin);
			pa.setPermission(this.permissionAdmin, true);
			this.playerPermissions.put(name, pa);
		}
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
		removePermission(event.getPlayer());
    }
    
    @Override
    public void onPlayerKick(PlayerKickEvent event) {
		removePermission(event.getPlayer());
    }
	
	public void AddAdmin(Player player) {
		String name = player.getName();
		if(this.adminList.contains(name))
			return;
		this.adminList.add(name);
		PermissionAttachment pa = player.addAttachment(this.plugin);
		pa.setPermission(this.permissionAdmin, true);
		this.playerPermissions.put(name, pa);
	}
	
	public void RemoveAdmin(Player player) {
		String name = player.getName();
		if(!this.adminList.contains(name))
			return;
		this.adminList.remove(name);
		if(!this.playerPermissions.containsKey(name))
			return;
		PermissionAttachment pa = this.playerPermissions.get(name);
		player.removeAttachment(pa);
		this.playerPermissions.remove(name);
	}
	
	
	protected boolean hasPermissionUse(Permissible permissible) {
		if(this.permissionUse == null) {
			return true;
		}
		return permissible.hasPermission(this.permissionUse);
	}
	protected boolean hasPermissionAdmin(Permissible permissible) {
		if(this.permissionAdmin == null) {
			if(permissible instanceof Player) {
				Player player = (Player)permissible;
				return player.isOp();
			}
			return false;
		}
		return permissible.hasPermission(this.permissionAdmin);
	}
	protected void removePermission(Player player) {
    	if(player != null) {
			String name = player.getName();
			if(this.playerPermissions.containsKey(name)) {
				PermissionAttachment pa = this.playerPermissions.get(name);
				player.removeAttachment(pa);
				this.playerPermissions.remove(name);
			}
		}
    }
	
	protected Permission permissionUse;
	protected Permission permissionAdmin;
	protected List<String> adminList;
	protected Plugin plugin;
	protected HashMap<String, PermissionAttachment> playerPermissions;
	
}
