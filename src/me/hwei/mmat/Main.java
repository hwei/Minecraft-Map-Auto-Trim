package me.hwei.mmat;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jnbt.ByteArrayTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.NBTInputStream;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String basePathStr = null;
		int DilationSize = 3;
		
		
		for(int i=0; i<args.length; ++i) {
			if(args[i].equals("-w")) {
				if(++i >= args.length)
					break;
				basePathStr = args[i];
			} else if(args[i].equals("-d")) {
				if(++i >= args.length)
					break;
				try {
					int pd = Integer.parseInt(args[i]);
					if(pd >= 0) {
						DilationSize = pd;
					}
				} catch (NumberFormatException e) {
				}
			}
		}
		
		if(basePathStr == null) {
			System.out.println("Minecraft map auto trim. Author: HWei.");
			System.out.println("Usage: mmat -w <world path> [-d <dilation size>]");
			return;
		}
		
		
		File basePath = new File(basePathStr);
		Pattern filenamePatten = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mcr");
		Comparator<IntPair> comparator = new Comparator<IntPair>() {
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
		};
		
		TreeSet<IntPair> remainChunkSet = new TreeSet<IntPair>(comparator);
		TreeSet<IntPair> allChunkSet = new TreeSet<IntPair>(comparator);
		
		try {
			File regionPath = new File(basePath, "region");
			System.out.println("Check chunks to remain...");
			for(File file : regionPath.listFiles()) {
				Matcher matcher = filenamePatten.matcher(file.getName());
				if(matcher.find()) {
					RegionFile rfile = new RegionFile(file);
					for(int x=0; x<32; ++x) {
						for(int z=0; z<32; ++z) {
							DataInputStream dis = rfile.getChunkDataInputStream(x, z);
							if(dis == null)
								continue;
							NBTInputStream nbtis = new NBTInputStream(dis);
							CompoundTag levelTag = (CompoundTag)((CompoundTag)nbtis.readTag()).getValue().get("Level");
							IntTag xPosTag = (IntTag)levelTag.getValue().get("xPos");
							IntTag zPosTag = (IntTag)levelTag.getValue().get("zPos");
							int cx = xPosTag.getValue();
							int cz = zPosTag.getValue();
							IntPair pos = new IntPair(cx, cz);
							allChunkSet.add(pos);
							ByteArrayTag blocksTag = (ByteArrayTag)levelTag.getValue().get("Blocks");
							if(toRemain(blocksTag.getValue())) {
								//dilate remain region
								for(int dx=-DilationSize; dx<=DilationSize; ++dx) {
									for(int dz=-DilationSize; dz<=DilationSize; ++dz) {
										remainChunkSet.add(new IntPair(cx+dx, cz+dz));
									}
								}
							}
							dis.close();
						}
					}
					rfile.close();
				}
			}
			int allChunksCount = allChunkSet.size();
			int remainChunksCount = remainChunkSet.size();
			System.out.println("All chunks count: " + allChunksCount);
			System.out.println("Remain chunks count: " + remainChunksCount);
			allChunkSet.removeAll(remainChunkSet);
			int deleteChunksCount = allChunkSet.size();
			System.out.println("Delete chunks count: " + deleteChunksCount);
			
			for(IntPair pos : allChunkSet) {
				int cx = pos.getLeft();
				int cz = pos.getRight();
				RegionFileCache.deleteChunkData(basePath, cx, cz);
			}
			
			RegionFileCache.clear();
			
			System.out.println("All chunks count: " + allChunksCount);
			System.out.println("Delete chunks count: " + deleteChunksCount);
			System.out.println("Remain chunks count: " + remainChunksCount);
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	private static class IntPair {
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
	
	// whether to remain a chunk.
	private static boolean toRemain(byte[] blocksData) {
		for(int x=0; x<16; ++x) {
			for(int y=0; y<128; ++y) {
				for(int z=0; z<16; ++z) {
					byte blockID = blocksData[ y + ( z * 128 + ( x * 128 * 16 ) ) ];
					Material material = Material.getMaterial((int)blockID);
					switch(material) {
						case GLASS:
						case TORCH:
						case JACK_O_LANTERN:
						case GLOWSTONE:
						case SIGN_POST:
						case WALL_SIGN:
							return true;
						default:
							break;
					}
				}
			}
		}
		
		return false;
	}

}
