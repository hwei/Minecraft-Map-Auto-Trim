package me.hwei.bukkit.scoreplugin;

import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Server;

public class ScoreOutput {
	public ScoreOutput(Logger consoleLogger, String pluginName, Server server) {
		this.consoleLogger = consoleLogger;
		this.server = server;
		this.prefix_normal = "[" + pluginName + "] ";
		this.prefix_color = "[" + ChatColor.YELLOW + pluginName + ChatColor.WHITE + "] ";
	}
	public void ToCommandSender(CommandSender sender, String message) {
		if(sender instanceof Player) {
			sender.sendMessage(this.prefix_color + message);
		} else {
			sender.sendMessage(this.prefix_normal + ChatColor.stripColor(message));
		}
	}
	public void ToPlayer(Player player, String message) {
		player.sendMessage(this.prefix_color + message);
	}
	public void ToPlayer(String name, String message) {
		Player player = this.server.getPlayer(name);
		if(player == null)
			return;
		this.ToPlayer(player, message);
	}
	public void ToConsole(String message) {
		this.consoleLogger.info(this.prefix_normal + message);
	}
	public void ToAll(String message) {
		this.server.broadcastMessage(this.prefix_color + message);
	}
	protected String prefix_color;
	protected String prefix_normal;
	protected Logger consoleLogger;
	protected Server server;
}
