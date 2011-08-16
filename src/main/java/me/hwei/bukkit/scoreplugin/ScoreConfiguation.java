package me.hwei.bukkit.scoreplugin;

import java.util.List;

import org.bukkit.util.config.Configuration;

public class ScoreConfiguation {
	public ScoreConfiguation(Configuration configuation) {
		this.configuation = configuation;
		this.configuation.load();
		this.price = this.configuation.getDouble("price", 0.0);
		this.viewer_max_reward = this.configuation.getDouble("viewer_max_reward", 0.0);
		this.auther_max_reward = this.configuation.getDouble("auther_max_reward", 0.0);
		this.viewer_score_threshold = this.configuation.getDouble("viewer_score_threshold", 0.0);
		this.auther_score_threshold = this.configuation.getDouble("auther_score_threshold", 0.0);
		this.adminList = this.configuation.getStringList("admins", null);
	}
	
	public void Save() {
		this.configuation.setProperty("price", this.price);
		this.configuation.setProperty("viewer_max_reward", this.viewer_max_reward);
		this.configuation.setProperty("auther_max_reward", this.auther_max_reward);
		this.configuation.setProperty("viewer_score_threshold", this.viewer_score_threshold);
		this.configuation.setProperty("auther_score_threshold", this.auther_score_threshold);
		this.configuation.setProperty("admins", adminList);
		this.configuation.save();
	}
	
	

	protected Configuration configuation = null;
	protected double price = 0.0;
	protected double viewer_max_reward = 0.0;
	protected double auther_max_reward = 0.0;
	protected double viewer_score_threshold = 0.0;
	protected double auther_score_threshold = 0.0;
	protected List<String> adminList = null;
	
	public List<String> getAdminList() {
		return adminList;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getViewer_max_reward() {
		return viewer_max_reward;
	}

	public void setViewer_max_reward(double viewer_max_reward) {
		this.viewer_max_reward = viewer_max_reward;
	}

	public double getAuther_max_reward() {
		return auther_max_reward;
	}

	public void setAuther_max_reward(double auther_max_reward) {
		this.auther_max_reward = auther_max_reward;
	}

	public double getViewer_score_threshold() {
		return viewer_score_threshold;
	}

	public void setViewer_score_threshold(double viewer_score_threshold) {
		this.viewer_score_threshold = viewer_score_threshold;
	}

	public double getAuther_score_threshold() {
		return auther_score_threshold;
	}

	public void setAuther_score_threshold(double auther_score_threshold) {
		this.auther_score_threshold = auther_score_threshold;
	}


	
}
