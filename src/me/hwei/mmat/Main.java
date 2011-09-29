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
		boolean[] preservedIds = new boolean[256];
		boolean userDefinedPreservedId = false;
		
		
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
			} else if(args[i].equals("-p")) {
				if(++i >= args.length)
					break;
				String[] ids = args[i].split(",");
				for(String id : ids) {
					try {
						byte blockId = Byte.parseByte(id, 10);
						preservedIds[blockId] = true;
					} catch (NumberFormatException e) {
					}
				}
				userDefinedPreservedId = true;
			}
		}
		
		if(userDefinedPreservedId == false) {
			preservedIds[5] = true;
			preservedIds[19] = true;
			preservedIds[20] = true;
			preservedIds[22] = true;
			preservedIds[23] = true;
			preservedIds[25] = true;
			preservedIds[26] = true;
			preservedIds[27] = true;
			preservedIds[28] = true;
			preservedIds[29] = true;
			preservedIds[33] = true;
			preservedIds[34] = true;
			preservedIds[35] = true;
			preservedIds[36] = true;
			preservedIds[41] = true;
			preservedIds[42] = true;
			preservedIds[43] = true;
			preservedIds[44] = true;
			preservedIds[45] = true;
			preservedIds[46] = true;
			preservedIds[47] = true;
			preservedIds[53] = true;
			preservedIds[55] = true;
			preservedIds[57] = true;
			preservedIds[59] = true;
			preservedIds[60] = true;
			preservedIds[63] = true;
			preservedIds[64] = true;
			preservedIds[65] = true;
			preservedIds[66] = true;
			preservedIds[67] = true;
			preservedIds[68] = true;
			preservedIds[69] = true;
			preservedIds[70] = true;
			preservedIds[71] = true;
			preservedIds[72] = true;
			preservedIds[75] = true;
			preservedIds[76] = true;
			preservedIds[77] = true;
			preservedIds[80] = true;
			preservedIds[84] = true;
			preservedIds[85] = true;
			preservedIds[87] = true;
			preservedIds[88] = true;
			preservedIds[89] = true;
			preservedIds[91] = true;
			preservedIds[92] = true;
			preservedIds[93] = true;
			preservedIds[94] = true;
			preservedIds[96] = true;
		}
		
		if(basePathStr == null) {
			System.out.println("Minecraft map auto trim v0.2. Author: HWei.");
			System.out.println("Usage: java -jar mmat.jar -w <world path> [-d <dilation size>] [-p <user defined preserve block id list (spliter: comma)>]");
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
							if(toRemain(blocksTag.getValue(), preservedIds)) {
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
	private static boolean toRemain(byte[] blocksData, boolean[] preservedIds) {
		for(int x=0; x<16; ++x) {
			int a = x * 128 * 16;
			for(int z=0; z<16; ++z) {
				int b = z * 128 + a;
				for(int y=0; y<128; ++y) {
					byte blockID = blocksData[ y + b ];
					if(preservedIds[blockID])
						return true;
				}
			}
		}
		return false;
	}

}
