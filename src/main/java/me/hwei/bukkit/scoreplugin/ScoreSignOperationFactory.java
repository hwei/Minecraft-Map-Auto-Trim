package me.hwei.bukkit.scoreplugin;

import java.util.List;

import me.hwei.bukkit.scoreplugin.ScorePermissionManager.ScorePermissionType;
import me.hwei.bukkit.scoreplugin.data.Score;
import me.hwei.bukkit.scoreplugin.data.ScoreAggregate;
import me.hwei.bukkit.scoreplugin.data.Work;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.SqlUpdate;


public class ScoreSignOperationFactory {
	public ScoreSignOperationFactory(
			ScoreOutput output,
			EbeanServer database,
			ScorePermissionManager permissionManager,
			ScoreConfiguation configuation,
			ScoreMoneyManager moneyManager) {
		this.output = output;
		this.database = database;
		this.permissionManager = permissionManager;
		this.configuation = configuation;
		this.moneyManager = moneyManager;
	}
	public IScoreSignOperation Create(String[] args, Permissible permissible) {
		if(args.length == 0)
			return null;
		Player player = null;
		if(permissible instanceof Player) {
			player = (Player)permissible;
		} else {
			return null;
		}
		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("info")) {
				if(!permissionManager.HasPermission(player, ScorePermissionType.USE))
					return null;
				return new OperationInfo(player);
			}
			if(args[0].equalsIgnoreCase("open")) {
				if(!permissionManager.HasPermission(player, ScorePermissionType.ADMIN))
					return null;
				return new OperationOpen(player);
			}
			if(args[0].equalsIgnoreCase("admin")) {
				if(!permissionManager.HasPermission(player, ScorePermissionType.ADMIN))
					return null;
				return new OperationAdmininfo(player);
			}
			if(args[0].equalsIgnoreCase("unset")) {
				if(!permissionManager.HasPermission(player, ScorePermissionType.ADMIN))
					return null;
				return new OperationUnsetForcedScore(player);
			}
			if(args[0].equalsIgnoreCase("clear")) {
				if(!permissionManager.HasPermission(player, ScorePermissionType.ADMIN))
					return null;
				return new OperationClear(player);
			}
			if(args[0].equalsIgnoreCase("close")) {
				if(!permissionManager.HasPermission(player, ScorePermissionType.ADMIN))
					return null;
				return new OperationClose(player);
			}
			if(args[0].equalsIgnoreCase("remove")) {
				if(!permissionManager.HasPermission(player, ScorePermissionType.ADMIN))
					return null;
				return new OperationRemove(player);
			}
			
			if(!permissionManager.HasPermission(player, ScorePermissionType.USE))
				return null;
			try {
				double scoreNumber = Double.parseDouble(args[0]);
				if(scoreNumber >= 0.0 && scoreNumber <= 10.0)
					return new OperationScore(player, scoreNumber);
			} catch (NumberFormatException e) {
			}
		} else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("set")) {
				if(!permissionManager.HasPermission(player, ScorePermissionType.ADMIN))
					return null;
				try {
					double forcedScoreNumber = Double.parseDouble(args[1]);
					if(forcedScoreNumber >= 0.0 && forcedScoreNumber <= 10.0)
						return new OperationSetForcedScore(player, forcedScoreNumber);
				} catch (NumberFormatException e) {
				}
				return null;
			}
			if(args[0].equalsIgnoreCase("maxreward")) {
				if(!permissionManager.HasPermission(player, ScorePermissionType.ADMIN))
					return null;
				try {
					double maxReward = Double.parseDouble(args[1]);
					return new OperationSetMaxReward(player, maxReward);
				} catch (NumberFormatException e) {
				}
				return null;
			}
		}
		return null;
	}
	protected double calcAuthorReward(double score, double maxReward) {
		double score_threshold = this.configuation.getAuther_score_threshold();
		if(score > score_threshold) {
			return maxReward * (score - score_threshold) / (10.0 - score_threshold);
		} else {
			return 0.0;
		}
	}
	protected double calcViewerReward(double score, double viewer_score) {
		double diff = Math.abs(score - viewer_score);
		double max_reword = this.configuation.getViewer_max_reward();
		double score_threshold = this.configuation.getViewer_score_threshold();
		
		if(diff > score_threshold) {
			return 0.0;
		} else {
			return max_reword * (score_threshold - diff) / score_threshold;
		}
	}
	
	protected ScoreOutput output;
	protected EbeanServer database;
	protected ScorePermissionManager permissionManager;
	protected ScoreConfiguation configuation;
	protected ScoreMoneyManager moneyManager;
	protected final String signMark = "[Score]";
	
	protected Work ReadInfoFromSign(Sign sign) {
		if(sign == null)
			return null;
		String signMark = sign.getLine(0);
		if(!signMark.equalsIgnoreCase(this.signMark)
				&& !signMark.equalsIgnoreCase("" + ChatColor.DARK_BLUE + this.signMark)) {
			return null;
		}
		Work work = new Work();
		work.setName(sign.getLine(1));
		String authorLine = sign.getLine(3);
		if(authorLine.startsWith(ChatColor.DARK_GRAY.toString())) {
			work.setAuthor(authorLine.substring(2));
		} else {
			work.setAuthor(authorLine);
		}
		work.setPos_x(sign.getX());
		work.setPos_y(sign.getY());
		work.setPos_z(sign.getZ());
		return work;
	}
	protected void setSign(Work work, Sign sign) {
		sign.setLine(0, "" + ChatColor.DARK_BLUE + this.signMark);
		sign.setLine(1, work.getName());
		Double score = work.getScore();
		if(work.getReward() == null) {
			sign.setLine(2, "" + ChatColor.DARK_RED + "pending");
		} else {
			sign.setLine(2, String.format("%.2f", score));
		}

		sign.setLine(3, "" + ChatColor.DARK_GRAY + work.getAuthor());
		sign.update();
	}
	
	protected class OperationInfo implements IScoreSignOperation {
		public OperationInfo(Player player) {
			this.player = player;
		}
		@Override
		public boolean Execute(Sign sign) {
			if(sign == null)
				return false;
			Work infoFromSign = ScoreSignOperationFactory.this.ReadInfoFromSign(sign);
			if(infoFromSign == null) {
				return false;
			}
			Work work = ScoreSignOperationFactory.this.database
					.find(Work.class)
					.where()
					.eq("pos_x", infoFromSign.getPos_x())
					.eq("pos_y", infoFromSign.getPos_y())
					.eq("pos_z", infoFromSign.getPos_z())
					.findUnique();
			if(work == null) {
				sign.setLine(2, "");
				sign.setLine(3, "" + ChatColor.DARK_GRAY + this.player.getName());
				sign.update();
				ScoreSignOperationFactory.this.output.ToPlayer(this.player, "This score sign has not been opened yet.");
				return true;
			}
			ScoreSignOperationFactory.this.setSign(work, sign);
			Score score = ScoreSignOperationFactory.this.database
					.find(Score.class)
					.where()
					.eq("viewer", this.player.getName())
					.eq("work_id", work.getWork_id())
					.findUnique();
			if(work.getReward() == null) {
				int viewerNumber = ScoreSignOperationFactory.this.database
						.find(Score.class)
						.where()
						.eq("work_id", work.getWork_id())
						.findRowCount();
				ScoreSignOperationFactory.this.output.ToPlayer(this.player,
						String.format("This score sign is open. "
								+ ChatColor.GREEN + "%d" + ChatColor.WHITE + " players have given it a score.", viewerNumber));
				if(score == null) {
					ScoreSignOperationFactory.this.output.ToPlayer(this.player, "To give a score, use '/scr <score>'.");
					return true;
				} else {
					ScoreSignOperationFactory.this.output.ToPlayer(this.player, String.format(
							"You have given it a score of " + ChatColor.GREEN + "%.2f" + ChatColor.WHITE + ".", score.getScore()));
					return true;
				}
			} else {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player,
						"This score sign has already been closed. Author has won "
						+ ChatColor.GREEN
						+ ScoreSignOperationFactory.this.moneyManager.Format(work.getReward())
						+ ChatColor.WHITE + ".");
				if(score != null) {
					ScoreSignOperationFactory.this.output.ToPlayer(this.player, 
							"You have given it a score of "
							+ ChatColor.GREEN
							+ String.format("%.2f", score.getScore())
							+ ChatColor.WHITE
							+ ", and won "+ ChatColor.GREEN
							+ ScoreSignOperationFactory.this.moneyManager.Format(score.getReward())
							+ ChatColor.WHITE + "."
							);
					return true;
				}
				return true;
			}

		}
		@Override
		public String GetHint() {
			return this.hint;
		}
		protected String getWorkInfoString(Work work) {
			return "NAME: " + ChatColor.YELLOW + work.getName() + ChatColor.WHITE
					+ " , AUTHOR: " + ChatColor.YELLOW + work.getAuthor() + ChatColor.WHITE
					+ " , SCORE: " + (work.getScore() == null ?
					ChatColor.RED + "pending"
					: ChatColor.YELLOW.toString() + work.getScore())
					+ ChatColor.WHITE;
		}

		protected Player player;
		protected final String hint = "Punch a score sign to view info.";
		
	}
	
	protected class OperationOpen implements IScoreSignOperation {
		public OperationOpen(Player player) {
			this.player = player;
		}
		
		@Override
		public boolean Execute(Sign sign) {
			if(sign == null)
				return false;
			Work infoFromSign = ScoreSignOperationFactory.this.ReadInfoFromSign(sign);
			if(infoFromSign == null) {
				return false;
			}
			Work work = ScoreSignOperationFactory.this.database
					.find(Work.class)
					.where()
					.eq("pos_x", infoFromSign.getPos_x())
					.eq("pos_y", infoFromSign.getPos_y())
					.eq("pos_z", infoFromSign.getPos_z())
					.findUnique();
			if(work == null) {
				work = infoFromSign;
				work.setMax_reward(ScoreSignOperationFactory.this.configuation.getAuther_max_reward());
				work.setPos_x(sign.getX());
				work.setPos_y(sign.getY());
				work.setPos_z(sign.getZ());
				ScoreSignOperationFactory.this.database.save(work);
				ScoreSignOperationFactory.this.setSign(work, sign);
				ScoreSignOperationFactory.this.output.ToAll("A new score sign opened! Name: "
						+ ChatColor.GREEN + work.getName() + ChatColor.WHITE
						+ ", Author: " + ChatColor.GREEN + work.getAuthor() + ChatColor.WHITE + ".");
			} else {
				ScoreSignOperationFactory.this.setSign(work, sign);
				ScoreSignOperationFactory.this.output.ToPlayer(this.player, "Already open.");
			}
			return false;
		}

		@Override
		public String GetHint() {
			return this.hint;
		}
		
		protected final String hint = "Punch a score sign to open it.";
		protected Player player;
	}
	
	protected class OperationScore implements IScoreSignOperation {
		public OperationScore(Player player, double scoreNumber) {
			this.player = player;
			this.scoreNumber = scoreNumber;
		}

		@Override
		public boolean Execute(Sign sign) {
			if(sign == null)
				return false;
			Work infoFromSign = ScoreSignOperationFactory.this.ReadInfoFromSign(sign);
			if(infoFromSign == null) {
				return false;
			}
			Work work = ScoreSignOperationFactory.this.database
					.find(Work.class)
					.where()
					.eq("pos_x", infoFromSign.getPos_x())
					.eq("pos_y", infoFromSign.getPos_y())
					.eq("pos_z", infoFromSign.getPos_z())
					.findUnique();
			if(work == null) {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player, "This score sign has not been opened yet.");
				return true;
			}
			if(work.getReward() != null) {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player,
						"This score sign has already been closed. Author has won "
						+ ChatColor.GREEN
						+ ScoreSignOperationFactory.this.moneyManager.Format(work.getReward())
						+ ChatColor.WHITE + ".");
				return true;
			}
			Score score = ScoreSignOperationFactory.this.database
					.find(Score.class)
					.where()
					.eq("viewer", this.player.getName())
					.eq("work_id", work.getWork_id())
					.findUnique();
			if(score == null) {
				double price = ScoreSignOperationFactory.this.configuation.getPrice();
				ScoreMoneyManager moneyManager = ScoreSignOperationFactory.this.moneyManager;
				if(moneyManager.TakeMoney(player.getName(), price)) {
					score = new Score();
					score.setScore(this.scoreNumber);
					score.setWork_id(work.getWork_id());
					score.setViewer(this.player.getName());
					ScoreSignOperationFactory.this.database.save(score);
					ScoreSignOperationFactory.this.output.ToPlayer(this.player, 
							"You have given a score of "
							+ ChatColor.GREEN + String.format("%.2f", this.scoreNumber) + ChatColor.WHITE
							+ " , and paid "
							+ ChatColor.GREEN + ScoreSignOperationFactory.this.moneyManager.Format(price) + ChatColor.WHITE
							+ ".");
					ScoreSignOperationFactory.this.output.ToAll(
							"" + ChatColor.GREEN + this.player.getName() + ChatColor.WHITE
							+ " has given a score to "
							+ ChatColor.GREEN + work.getName() + ChatColor.WHITE
							+ " ( author: " + ChatColor.GREEN + work.getAuthor() + ChatColor.WHITE
							+ " ).");
				} else {
					ScoreSignOperationFactory.this.output.ToPlayer(this.player, "You do not have enough money to give score.");
				}
				return true;
			} else {
				double oldScoreNumber = score.getScore();
				score.setScore(this.scoreNumber);
				ScoreSignOperationFactory.this.database.save(score);
				ScoreSignOperationFactory.this.output.ToPlayer(this.player,
						String.format("Changed score from "
						+ ChatColor.GREEN + "%.2f" + ChatColor.WHITE
						+ " to " + ChatColor.GREEN + "%.2f" + ChatColor.WHITE + ".",
						oldScoreNumber, this.scoreNumber));
				return true;
			}
		}

		@Override
		public String GetHint() {
			return this.hint;
		}
		protected final String hint = "Punch a score sign to give it score.";
		protected Player player;
		protected double scoreNumber;
	}
	
	protected class OperationAdmininfo implements IScoreSignOperation {
		public OperationAdmininfo(Player player) {
			this.player = player;
		}
		
		@Override
		public boolean Execute(Sign sign) {
			if(sign == null)
				return false;
			Work infoFromSign = ScoreSignOperationFactory.this.ReadInfoFromSign(sign);
			if(infoFromSign == null) {
				return false;
			}
			Work work = ScoreSignOperationFactory.this.database
					.find(Work.class)
					.where()
					.eq("pos_x", infoFromSign.getPos_x())
					.eq("pos_y", infoFromSign.getPos_y())
					.eq("pos_z", infoFromSign.getPos_z())
					.findUnique();
			if(work == null) {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player, "This score sign has not been opened yet. User '/scr open' to open it.");
				return true;
			}
			if(work.getReward() == null) {
				int viewerNumber = ScoreSignOperationFactory.this.database
						.find(Score.class)
						.where()
						.eq("work_id", work.getWork_id())
						.findRowCount();
				ScoreSignOperationFactory.this.output.ToPlayer(this.player,
						String.format("This score sign is open. "
								+ ChatColor.GREEN + "%d" + ChatColor.WHITE + " players have given it a score.", viewerNumber));
				ScoreAggregate scoreAgg = null;
				if(viewerNumber == 0) {
					scoreAgg = new ScoreAggregate();
					scoreAgg.setAverage(0.0);
					scoreAgg.setMax(0.0);
					scoreAgg.setMin(0.0);
				} else {
					String sql
						= "select avg(score) as average, min(score) as min, max(score) as max, sum(score) as sum "
						+ "from scores";
					RawSql rawSql = RawSqlBuilder.parse(sql).create();
					scoreAgg = ScoreSignOperationFactory.this.database
						.find(ScoreAggregate.class)
						.setRawSql(rawSql)
						.where()
						.eq("work_id", work.getWork_id())
						.findUnique();
				}
				String forcedScoreString = work.getScore() == null ? "none" :
					String.format("%.2f", work.getScore());
				double authorWillWin = work.getScore() == null ?
							ScoreSignOperationFactory.this.calcAuthorReward(scoreAgg.getAverage(), work.getMax_reward()) :
							ScoreSignOperationFactory.this.calcAuthorReward(work.getScore(), work.getMax_reward());
				
				ScoreSignOperationFactory.this.output.ToPlayer(this.player,
						String.format("AVG: " + ChatColor.GREEN + "%.2f" + ChatColor.WHITE
								+ ", MIN " + ChatColor.GREEN + "%.2f" + ChatColor.WHITE
								+ ", MAX " + ChatColor.GREEN + "%.2f" + ChatColor.WHITE
								+ ", SUM " + ChatColor.GREEN + "%.2f" + ChatColor.WHITE
								+ ", FORCE SCORE: "
								+ ChatColor.GREEN + forcedScoreString + ChatColor.WHITE
								+ ", MAX REWARD: " + ChatColor.GREEN + "%.2f" + ChatColor.WHITE
								+ ", AUTHOR WILL WIN: " + ChatColor.GREEN + "%.2f" + ChatColor.WHITE
								+ ".",
								scoreAgg.getAverage(),
								scoreAgg.getMin(),
								scoreAgg.getMax(),
								scoreAgg.getSum(),
								work.getMax_reward(),
								authorWillWin));
			} else {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player,
						"This score sign has already been closed. Author has won "
						+ ChatColor.GREEN
						+ ScoreSignOperationFactory.this.moneyManager.Format(work.getReward())
						+ ChatColor.WHITE + ".");
				return true;
			}
			return false;
		}

		@Override
		public String GetHint() {
			return this.hint;
		}
		protected final String hint = "Punch a score sign to view info for admin.";
		protected Player player;
	}
	
	protected class OperationSetForcedScore implements IScoreSignOperation {
		public OperationSetForcedScore(Player player, double scoreNumber) {
			this.player = player;
			this.scoreNumber = scoreNumber;
		}
		
		@Override
		public boolean Execute(Sign sign) {
			if(sign == null)
				return false;
			Work infoFromSign = ScoreSignOperationFactory.this.ReadInfoFromSign(sign);
			if(infoFromSign == null) {
				return false;
			}
			Work work = ScoreSignOperationFactory.this.database
					.find(Work.class)
					.where()
					.eq("pos_x", infoFromSign.getPos_x())
					.eq("pos_y", infoFromSign.getPos_y())
					.eq("pos_z", infoFromSign.getPos_z())
					.findUnique();
			if(work == null) {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player, "This score sign has not been opened yet. User '/scr open' to open it.");
				return true;
			}
			if(work.getReward() == null) {
				Double oldForcedScore = work.getScore();
				work.setScore(this.scoreNumber);
				ScoreSignOperationFactory.this.database.save(work);
				if(oldForcedScore == null) {
					ScoreSignOperationFactory.this.output.ToPlayer(this.player, 
							String.format("Set forced score " + ChatColor.GREEN + "%.2f" + ChatColor.WHITE + " .", this.scoreNumber));
				} else {
					ScoreSignOperationFactory.this.output.ToPlayer(this.player, 
							String.format("Change forced score from "
									+ ChatColor.GREEN + "%.2f" + ChatColor.WHITE
									+ " to "
									+ ChatColor.GREEN + "%.2f" + ChatColor.WHITE
									+ " .", oldForcedScore, this.scoreNumber));
				}
				return true;
			} else {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player,
						"This score sign has already been closed. Author has won "
						+ ChatColor.GREEN
						+ ScoreSignOperationFactory.this.moneyManager.Format(work.getReward())
						+ ChatColor.WHITE + ".");
				return true;
			}
		}

		@Override
		public String GetHint() {
			return this.hint;
		}
		protected final String hint = "Punch a score sign to set forced score.";
		protected Player player;
		protected double scoreNumber;
		
	}
	
	protected class OperationUnsetForcedScore implements IScoreSignOperation {
		public OperationUnsetForcedScore(Player player) {
			this.player = player;
		}
		
		@Override
		public boolean Execute(Sign sign) {
			if(sign == null)
				return false;
			Work infoFromSign = ScoreSignOperationFactory.this.ReadInfoFromSign(sign);
			if(infoFromSign == null) {
				return false;
			}
			Work work = ScoreSignOperationFactory.this.database
					.find(Work.class)
					.where()
					.eq("pos_x", infoFromSign.getPos_x())
					.eq("pos_y", infoFromSign.getPos_y())
					.eq("pos_z", infoFromSign.getPos_z())
					.findUnique();
			if(work == null) {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player, "This score sign has not been opened yet. User '/scr open' to open it.");
				return true;
			}
			if(work.getReward() == null) {
				work.setScore(null);
				ScoreSignOperationFactory.this.database.save(work);
				String.format("Unset forced score.");
				return true;
			} else {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player,
						"This score sign has already been closed. Author has won "
						+ ChatColor.GREEN
						+ ScoreSignOperationFactory.this.moneyManager.Format(work.getReward())
						+ ChatColor.WHITE + ".");
				return true;
			}
		}

		@Override
		public String GetHint() {
			return this.hint;
		}
		protected final String hint = "Punch a score sign to unset forced score.";
		protected Player player;
	
	}
	
	protected class OperationClear implements IScoreSignOperation {
		public OperationClear(Player player) {
			this.player = player;
		}
		
		@Override
		public boolean Execute(Sign sign) {
			if(sign == null)
				return false;
			Work infoFromSign = ScoreSignOperationFactory.this.ReadInfoFromSign(sign);
			if(infoFromSign == null) {
				return false;
			}
			Work work = ScoreSignOperationFactory.this.database
					.find(Work.class)
					.where()
					.eq("pos_x", infoFromSign.getPos_x())
					.eq("pos_y", infoFromSign.getPos_y())
					.eq("pos_z", infoFromSign.getPos_z())
					.findUnique();
			if(work == null) {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player, "This score sign has not been opened yet. User '/scr open' to open it.");
				return true;
			}
			if(work.getReward() == null) {
				String sql = "delete from scores where work_id = :work_id";
				SqlUpdate delete = ScoreSignOperationFactory.this.database.createSqlUpdate(sql);
				delete.setParameter("work_id", work.getWork_id());
				ScoreSignOperationFactory.this.database.execute(delete);
				ScoreSignOperationFactory.this.output.ToPlayer(this.player, "Cleared all scores from viewers.");
				return true;
			} else {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player,
						"This score sign has already been closed. Author has won "
						+ ChatColor.GREEN
						+ ScoreSignOperationFactory.this.moneyManager.Format(work.getReward())
						+ ChatColor.WHITE + ".");
				return true;
			}
		}

		@Override
		public String GetHint() {
			return this.hint;
		}
		protected final String hint = "Punch a score sign to clear all scores from viewers.";
		protected Player player;
	
	}
	
	protected class OperationClose implements IScoreSignOperation {
		public OperationClose(Player player) {
			this.player = player;
		}
		
		@Override
		public boolean Execute(Sign sign) {
			if(sign == null)
				return false;
			Work infoFromSign = ScoreSignOperationFactory.this.ReadInfoFromSign(sign);
			if(infoFromSign == null) {
				return false;
			}
			Work work = ScoreSignOperationFactory.this.database
					.find(Work.class)
					.where()
					.eq("pos_x", infoFromSign.getPos_x())
					.eq("pos_y", infoFromSign.getPos_y())
					.eq("pos_z", infoFromSign.getPos_z())
					.findUnique();
			if(work == null) {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player, "This score sign has not been opened yet. User '/scr open' to open it.");
				return true;
			}
			ScoreOutput output = ScoreSignOperationFactory.this.output;
			if(work.getReward() == null) {
				Double score = work.getScore();
				if(score == null) {
					int viewerCount = ScoreSignOperationFactory.this.database
							.find(Score.class).findRowCount();
					if(viewerCount == 0) {
						output.ToPlayer(this.player, "No player gives it score. Can not close it. Can also use '/scr set' to give it a forced score.");
						return true;
					}
					String sql
						= "select avg(score) as average "
						+ "from scores";
					RawSql rawSql = RawSqlBuilder.parse(sql).create();
					ScoreAggregate scoreAgg = ScoreSignOperationFactory.this.database
						.find(ScoreAggregate.class)
						.setRawSql(rawSql)
						.fetch("average")
						.where()
						.eq("work_id", work.getWork_id())
						.findUnique();
					score = scoreAgg.getAverage();
				}
				double autherMaxReward = ScoreSignOperationFactory.this.configuation.getAuther_max_reward();
				double autherReward = ScoreSignOperationFactory.this.calcAuthorReward(score, autherMaxReward);
				ScoreMoneyManager moneyManager = ScoreSignOperationFactory.this.moneyManager;
				work.setScore(score);
				work.setReward(autherReward);
				ScoreSignOperationFactory.this.database.save(work);
				if(moneyManager.GiveMoney(work.getAuthor(), autherReward)) {
					output.ToAll("The score of "
							+ ChatColor.GREEN + work.getName() + ChatColor.WHITE
							+ " ( author: " + ChatColor.GREEN + work.getAuthor() + ChatColor.WHITE + " ) "
							+ " is "
							+ ChatColor.GREEN + String.format("%.2f", score) + ChatColor.WHITE
							+ ". " + ChatColor.GREEN + work.getAuthor() + ChatColor.WHITE + " has won "
							+ ChatColor.GREEN + moneyManager.Format(autherReward) + ChatColor.WHITE
							+ ".");
				}
				List<Score> scoreList = ScoreSignOperationFactory.this.database
					.find(Score.class)
					.where()
					.eq("work_id", work.getWork_id())
					.findList();
				Score bestViewerScore = null;
				for(Score viewerScore : scoreList) {
					double viewerReward = ScoreSignOperationFactory.this.calcViewerReward(score, viewerScore.getScore());
					viewerScore.setReward(viewerReward);
					if(moneyManager.GiveMoney(viewerScore.getViewer(), viewerReward)) {
						output.ToPlayer(viewerScore.getViewer(), "The score of work "
								+ ChatColor.GREEN + work.getName() + ChatColor.WHITE
								+ " is "
								+ ChatColor.GREEN + String.format("%.2f", score) + ChatColor.WHITE
								+ ". "
								+ "You have given score of "
								+ ChatColor.GREEN + String.format("%.2f", viewerScore.getScore()) + ChatColor.WHITE
								+ ". You have won "
								+ ChatColor.GREEN + moneyManager.Format(viewerReward) + ChatColor.WHITE
								+ ".");
						if(bestViewerScore == null || viewerScore.getReward() > bestViewerScore.getReward()) {
							bestViewerScore = viewerScore;
						}
					}
				}
				if(bestViewerScore != null) {
					output.ToAll("The best viewer is " + ChatColor.GREEN + bestViewerScore.getViewer() + ChatColor.WHITE
							+ ". He / she has given a score of " + ChatColor.GREEN + bestViewerScore.getScore() + ChatColor.WHITE
							+ " and won " + ChatColor.GREEN + moneyManager.Format(bestViewerScore.getReward()) + ChatColor.WHITE + ".");
				}
				
				ScoreSignOperationFactory.this.database.save(scoreList);
				
				return true;
			} else {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player,
						"This score sign has already been closed. Author has won "
						+ ChatColor.GREEN
						+ ScoreSignOperationFactory.this.moneyManager.Format(work.getReward())
						+ ChatColor.WHITE + ".");
				return true;
			}
		}

		@Override
		public String GetHint() {
			return this.hint;
		}
		protected final String hint = "Punch a score sign to close it and distribute rewards.";
		protected Player player;
	
	}
	
	protected class OperationRemove implements IScoreSignOperation {
		public OperationRemove(Player player) {
			this.player = player;
		}
		
		@Override
		public boolean Execute(Sign sign) {
			if(sign == null)
				return false;
			Work infoFromSign = ScoreSignOperationFactory.this.ReadInfoFromSign(sign);
			if(infoFromSign == null) {
				return false;
			}
			Work work = ScoreSignOperationFactory.this.database
					.find(Work.class)
					.where()
					.eq("pos_x", infoFromSign.getPos_x())
					.eq("pos_y", infoFromSign.getPos_y())
					.eq("pos_z", infoFromSign.getPos_z())
					.findUnique();
			if(work == null) {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player, "This score sign has not been opened yet.");
				return false;
			}
			String sql = "delete from scores where work_id = :work_id";
			SqlUpdate delete = ScoreSignOperationFactory.this.database.createSqlUpdate(sql);
			delete.setParameter("work_id", work.getWork_id());
			ScoreSignOperationFactory.this.database.execute(delete);
			ScoreSignOperationFactory.this.database.delete(work);
			sign.setLine(0, "");
			sign.setLine(1, "");
			sign.setLine(2, "");
			sign.setLine(3, "");
			sign.update();
			ScoreSignOperationFactory.this.output.ToPlayer(this.player, "Removed score functions.");
			return true;

		}

		@Override
		public String GetHint() {
			return this.hint;
		}
		protected final String hint = "Punch a score sign to remove score function.";
		protected Player player;
	
	}
	
	protected class OperationSetMaxReward implements IScoreSignOperation {
		public OperationSetMaxReward(Player player, double amount) {
			this.player = player;
			this.amount = amount;
		}
		
		@Override
		public boolean Execute(Sign sign) {
			if(sign == null)
				return false;
			Work infoFromSign = ScoreSignOperationFactory.this.ReadInfoFromSign(sign);
			if(infoFromSign == null) {
				return false;
			}
			Work work = ScoreSignOperationFactory.this.database
					.find(Work.class)
					.where()
					.eq("pos_x", infoFromSign.getPos_x())
					.eq("pos_y", infoFromSign.getPos_y())
					.eq("pos_z", infoFromSign.getPos_z())
					.findUnique();
			if(work == null) {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player, "This score sign has not been opened yet. User '/scr open' to open it.");
				return true;
			}
			if(work.getReward() == null) {
				work.setMax_reward(this.amount);
				ScoreSignOperationFactory.this.database.save(work);
				String.format("Set max reward to " + ChatColor.GREEN
						+ ScoreSignOperationFactory.this.moneyManager.Format(amount)
						+ ChatColor.WHITE + ".");
				return true;
			} else {
				ScoreSignOperationFactory.this.output.ToPlayer(this.player,
						"This score sign has already been closed. Author has won "
						+ ChatColor.GREEN
						+ ScoreSignOperationFactory.this.moneyManager.Format(work.getReward())
						+ ChatColor.WHITE + ".");
				return true;
			}
		}
		@Override
		public String GetHint() {
			return this.hint;
		}
		protected final String hint = "Punch a score sign to set max reward for author.";
		protected Player player;
		protected double amount;
	
	}
}
