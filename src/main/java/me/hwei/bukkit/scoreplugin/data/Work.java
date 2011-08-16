package me.hwei.bukkit.scoreplugin.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name = "works")
public class Work {
	@Id
	private int work_id;
    @NotNull
    private String name;
    @NotNull
    private String author;
    
    private Double score;
    
	public int getWork_id() {
		return work_id;
	}

	public void setWork_id(int work_id) {
		this.work_id = work_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public int getPos_x() {
		return pos_x;
	}

	public void setPos_x(int pos_x) {
		this.pos_x = pos_x;
	}

	public int getPos_y() {
		return pos_y;
	}

	public void setPos_y(int pos_y) {
		this.pos_y = pos_y;
	}

	public int getPos_z() {
		return pos_z;
	}

	public void setPos_z(int pos_z) {
		this.pos_z = pos_z;
	}

	public double getMax_reward() {
		return max_reward;
	}

	public void setMax_reward(double max_reward) {
		this.max_reward = max_reward;
	}

	public Double getReward() {
		return reward;
	}

	public void setReward(Double reward) {
		this.reward = reward;
	}

	@NotNull
	private int pos_x;
    @NotNull
    private int pos_y;
    @NotNull
    private int pos_z;
    @NotNull
    private double max_reward;

    private Double reward;
}
