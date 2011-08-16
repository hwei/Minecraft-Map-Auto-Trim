package me.hwei.bukkit.scoreplugin;

import org.bukkit.block.Sign;

public interface IScoreSignOperation {
	public boolean Execute(Sign sign);
	public String GetHint();
}
