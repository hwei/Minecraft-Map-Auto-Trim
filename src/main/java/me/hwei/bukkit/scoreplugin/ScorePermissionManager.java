package me.hwei.bukkit.scoreplugin;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

public class ScorePermissionManager {
	public ScorePermissionManager(List<Permission> permissions) {
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
	
	protected Permission permissionUse;
	protected Permission permissionAdmin;
}
