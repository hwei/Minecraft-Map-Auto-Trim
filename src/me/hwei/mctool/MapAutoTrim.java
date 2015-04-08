package me.hwei.mctool;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import me.hwei.mctool.world.ChunkHandle;
import me.hwei.mctool.world.WorldStorage;

public class MapAutoTrim {
	
	public static void main(String[] args) {
		Options opt = Options.ReadArgs(args);
		if(opt.mapPathStr == null) {
			printUsage();
			System.exit(1);
			return;
		}
		File mapFolder = new File(opt.mapPathStr);
		if(!mapFolder.exists()) {
			System.err.printf("Map folder does not exist: %s\n", mapFolder.getAbsolutePath());
			System.exit(1);
			return;
		}
		WorldStorage worldStorage = WorldStorage.load(mapFolder);
		if(worldStorage == null) {
			System.err.printf("Could not load: %s\n", mapFolder.getAbsolutePath());
			System.exit(1);
			return;
		} else {
			System.out.printf("Loaded world: %s\n", worldStorage.getLevelName());
		}
		ArrayList<ChunkHandle> allChunks = worldStorage.getAllChunks();
		System.out.printf("Found %d chunks.\n\n", allChunks.size());
		System.out.println("Using following parameters to trim:");
        System.out.println("Conditions:");

        for (Options.Condition condition : opt.conditions) {
            System.out.print("For Y range: ");
            if (condition.yRange == null) {
                System.out.print("(none)");
            } else {
                System.out.printf("yBegin=%d yEnd=%d", condition.yRange[0], condition.yRange[1]);
            }
            System.out.printf(",\n\tpreserved block ID list: ");
            for (int i = 0; i < condition.preservedIds.length; ++i) {
                if (condition.preservedIds[i]) {
                    System.out.print(i);
                    System.out.print(' ');
                }
            }
            System.out.println();
        }

		System.out.printf("Dilation size: %d\n", opt.dilationSize);
		System.out.print("Cut rectangle: ");
		if(opt.rect == null) {
			System.out.println("(none)");
		} else {
			System.out.printf("minX=%d maxX=%d minZ=%d maxZ=%d\n",
					opt.rect[0] * 16, opt.rect[1] * 16 + 15,
					opt.rect[2] * 16, opt.rect[3] * 16 + 15);
		} 

		System.out.print("Reading chunks... ");
		ProgressReporter progressReporter = new ProgressReporter(allChunks.size());
		
		ArrayList<ChunkAndMarks> allChunkAndMarks = new ArrayList<ChunkAndMarks>();
		Map<IntPair, ChunkAndMarks> chunkMap = new TreeMap<IntPair, ChunkAndMarks>(new IntPairComparator());
		Iterator<ChunkHandle> iterChunkHandle = allChunks.iterator();
		while(iterChunkHandle.hasNext()) {
			ChunkHandle chunkHandle = iterChunkHandle.next();
			ChunkAndMarks cm = new ChunkAndMarks();
			cm.chunkHandle = chunkHandle;
            for (Options.Condition condition : opt.conditions)
            {
                if(condition.yRange == null) {
                    cm.containPreservedBlock |= chunkHandle.hasAnyBlock(condition.preservedIds);
                } else {
                    cm.containPreservedBlock |= chunkHandle.hasAnyBlock(condition.preservedIds, condition.yRange[0], condition.yRange[1]);
                }
            }
			cm.nearPreservedChunk = cm.containPreservedBlock;
			IntPair pos = new IntPair(chunkHandle.getChunkX(), chunkHandle.getChunkZ());
			if(opt.rect != null) {
				if(pos.getLeft() < opt.rect[0] || pos.getLeft() > opt.rect[1]
						|| pos.getLeft() < opt.rect[2] || pos.getRight() > opt.rect[3]) {
					cm.outOfrange = true;
				}
			}
			chunkMap.put(pos, cm);
			allChunkAndMarks.add(cm);
			
			progressReporter.completOne();
		}
		System.out.println("Done.");
		
		if(opt.dilationSize > 0) {
			System.out.print("Dilating chunks... ");
			progressReporter = new ProgressReporter(allChunks.size());
			Iterator<Entry<IntPair, ChunkAndMarks>> iterEntry = chunkMap.entrySet().iterator();
			while(iterEntry.hasNext()) {
				Entry<IntPair, ChunkAndMarks> entry = iterEntry.next();
				if(entry.getValue().containPreservedBlock) {
					int cx = entry.getKey().getLeft();
					int cz = entry.getKey().getRight();
					for(int i=cx-opt.dilationSize; i<=cx+opt.dilationSize; ++i) {
						for(int j=cz-opt.dilationSize; j<=cz+opt.dilationSize; ++j) {
							IntPair dpos = new IntPair(i, j);
							ChunkAndMarks cm = chunkMap.get(dpos);
							if(cm != null && cm.outOfrange == false) {
								cm.nearPreservedChunk = true;
							}
						}
					}
				}
				progressReporter.completOne();
			}
			System.out.println("Done.");
		}
		
		int preservedCount = 0;
		Iterator<ChunkAndMarks> iterChunkAndMarks = null;
		iterChunkAndMarks = allChunkAndMarks.iterator();
		System.out.print("Deleting chunks... ");
		progressReporter = new ProgressReporter(allChunks.size());
		while(iterChunkAndMarks.hasNext()) {
			ChunkAndMarks cm = iterChunkAndMarks.next();
			if(cm.outOfrange == false && cm.nearPreservedChunk == true) {
				++preservedCount;
			} else {
				cm.chunkHandle.delete();
			}
			progressReporter.completOne();
		}
		System.out.println();
		worldStorage.close();
		System.out.println("Done.");
		System.out.printf("\nAll complete. Remain %d - %d = %d trunks. (total - toDelete = remain)\n",
				allChunkAndMarks.size(), allChunkAndMarks.size() - preservedCount, preservedCount);
		
	}
	
	static void printUsage() {
		System.out.println("Minecraft Map Auto Trim. Help you delete unnecessary trunks.");
		System.out.println("Version: 0.4.1, Author: hwei");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("\tjava -jar mmat.jar -w <world path> [-d <dilation size>] [-r <minX,maxX,minZ,maxZ>] [-p <id list>] [-y <yBegin,yEnd>] [-p <id list 2>] [-y <yBegin 2,yEnd 2>] ... [-p <id list N>] [-y <yBegin N,yEnd N>]");
        System.out.println("Where:");
        System.out.println("\t-w <world path>\tPath to the world folders");
        System.out.println("\t-d <dilation size>\tDilate preserved area to perserve more chunks around the chunks with \"perserve block\"");
        System.out.println("\t-p <id list>\tDefine a list of \"perserve block\".If a chunk contains any \"perserve block\", it will be preserved.");
        System.out.println("\t-r <minX,maxX,minZ,maxZ>\tIf specified, all the chunks outside this rectangle will be forced to delete.");
        System.out.println("\t-y <yBegin,yEnd>\tIf specified, only scan this range of height.");
        System.out.println("\tSpecify one or more pairs of -y and -p options to add a condition.  If any one of the conditions is met, the chunk is not deleted");
        System.out.println("Examples:");
        System.out.println("\tDilate by 3 chunks, remove all chunks outside a x/z 1000 block radius from the origin, otherwise delete all chunks not containing blocks with ids 63 and 68 from Y-level 64 and above");
        System.out.println("\tjava -jar mmat.jar -w ~/minecraft/world -d 3 -p 63,68 -r -1000,1000,-1000,1000 -y 64,256");
        System.out.println("\tDelete chunks not containing blocks with ids 63 and 68 Y-level 64 and above and also not containing blocks with id 138 Y-level 63 and below");
        System.out.println("\tjava -jar mmat.jar -w ~/minecraft/world -p 63,68 -y 64,256 -p 138 -y 0,63");
        System.exit(1);
	}
	
	static protected class Options
	{
        private static final int[] DefaultPreservedIds = {5,19,20,22,23,25,26,27,
                28,29,33,34,35,36,41,42,43,44,45,46,47,53,55,57,59,
                60,63,64,65,66,67,68,69,70,71,72,75,76,77,80,84,85,
                87,88,89,91,92,93,94,96};

        static protected class Condition
        {
            private boolean preservedIdsSet = false;

            public final boolean[] preservedIds = new boolean[4096];
            public int[] yRange= null;

            public void assignIds(int [] ids)
            {
                for (int id : ids) {
                    this.preservedIds[id] = true;
                }
                this.preservedIdsSet = true;
            }

            public boolean isPreservedIdsSet() { return this.preservedIdsSet; }
            public void finishUpDude()
            {
                if (!preservedIdsSet) {
                    assignIds(DefaultPreservedIds);
                }
            }
        }
		public String mapPathStr = null;
		public int dilationSize = 3;
        public List<Condition> conditions = new LinkedList<Condition>();
        public int[] rect = null;
		public static Options ReadArgs(String[] args) {
			Options opt = new Options();
            Condition condition = new Condition();
            opt.conditions.add(condition);

			for(int i=0; i<args.length; ++i) {
				if(args[i].equals("-w")) {
					if(++i >= args.length)
						break;
					opt.mapPathStr = args[i];
				} else if(args[i].equals("-d")) {
					if(++i >= args.length)
						break;
					try {
						int pd = Integer.parseInt(args[i]);
						if(pd >= 0) {
							opt.dilationSize = pd;
						}
					} catch (NumberFormatException e) {
					}
				} else if(args[i].equals("-p")) {
                    if (condition.isPreservedIdsSet())
                    {
                        condition.finishUpDude();
                        opt.conditions.add(condition = new Condition());
                    }

					if(++i >= args.length)
						break;
					String[] stringIds = args[i].split(",");
                    int[] ids = new int[stringIds.length];
                    for (int c = 0; c < stringIds.length; ++c)
                    {
                        try {
                            ids[c] = Short.parseShort(stringIds[c], 10);
                        } catch (NumberFormatException e) {
                        }
                    }
                    condition.assignIds(ids);

				} else if(args[i].equals("-r")) {
					if(++i >= args.length)
						break;
					String[] pos = args[i].split(",");
					if(pos.length != 4)
						break;
					int[] rect = new int[4];
					for(int j=0; j<4; ++j) {
						try {
							rect[j] = Integer.parseInt(pos[j], 10) / 16;
						} catch (NumberFormatException e) {
						}
					}
					opt.rect = rect;
				} else if(args[i].equals("-y")) {
                    if (condition.yRange != null)
                    {
                        condition.finishUpDude();
                        opt.conditions.add(condition = new Condition());
                    }

					if(++i >= args.length)
						break;
					String[] y = args[i].split(",");
					if(y.length != 2)
						break;
					int[] yRange = new int[2];
					for(int j=0; j<2; ++j) {
						try {
							yRange[j] = Integer.parseInt(y[j], 10);
						} catch (NumberFormatException e) {
						}
					}
					condition.yRange = yRange;
				}
			}

            condition.finishUpDude();

			return opt;
		}
	}
	
	protected static class IntPair {
		public IntPair(int left, int right) {
			this.left = left;
			this.right = right;
		}
		public int getLeft() {
			return this.left;
		}
		public int getRight() {
			return this.right;
		}
		private int left;
		private int right;
	}
	
	protected static class IntPairComparator implements Comparator<IntPair> {
		@Override
		public int compare(IntPair o1, IntPair o2) {
			if(o1.getLeft() < o2.getLeft()) {
				return -1;
			} else if(o1.getLeft() > o2.getLeft()) {
				return 1;
			} else if(o1.getRight() < o2.getRight()) {
				return -1;
			} else if(o1.getRight() > o2.getRight()) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	protected static class ChunkAndMarks {
		public ChunkHandle chunkHandle = null;
		public boolean containPreservedBlock = false;
		public boolean nearPreservedChunk = false;
		public boolean outOfrange = false;
	}
	
	protected static class ProgressReporter {
		public ProgressReporter(int totalWork) {
			this.totalWork = totalWork;
			this.doneWork = 0;
			this.progressRate = 0;
			System.out.print("0% ");
		}
		public void completOne() {
			++doneWork;
			int currentRate = doneWork * 100 / totalWork;
			if(currentRate == progressRate)
				return;
			progressRate = currentRate;
			System.out.printf("%d%% ", progressRate);
		}
		protected int totalWork;
		protected int doneWork;
		protected int progressRate;
	}
}
