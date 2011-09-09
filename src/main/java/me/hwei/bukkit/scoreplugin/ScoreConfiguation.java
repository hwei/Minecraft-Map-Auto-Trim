package me.hwei.bukkit.scoreplugin;

import org.bukkit.util.config.Configuration;

public class ScoreConfiguation {
	public ScoreConfiguation(Configuration configuation) {
		this.configuation = configuation;
		this.Load();
	}
	
	public void Load() {
		this.configuation.load();
		this.price = this.configuation.getDouble("price", 25.0);
		this.viewer_max_reward = this.configuation.getDouble("viewer_max_reward", 500.0);
		this.auther_max_reward = this.configuation.getDouble("auther_max_reward", 5000.0);
		this.viewer_score_threshold = this.configuation.getDouble("viewer_score_threshold", 1.0);
		this.auther_score_threshold = this.configuation.getDouble("auther_score_threshold", 6.0);
		this.tp_price = this.configuation.getDouble("tp_price", 50.0);
	}
	
	protected Configuration configuation = null;
	protected double price = 0.0;
	protected double viewer_max_reward = 0.0;
	protected double auther_max_reward = 0.0;
	protected double viewer_score_threshold = 0.0;
	protected double auther_score_threshold = 0.0;
	protected double tp_price = 0.0;
	

	public double getTp_price() {
		return tp_price;
	}

	public void setTp_price(double tp_price) {
		this.tp_price = tp_price;
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
