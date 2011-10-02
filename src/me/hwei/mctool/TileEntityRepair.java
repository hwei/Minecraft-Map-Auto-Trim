package me.hwei.mctool;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jnbt.ByteArrayTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.ListTag;
import org.jnbt.StringTag;
import org.jnbt.NBTInputStream;
import org.jnbt.Tag;

public class TileEntityRepair {
	public static void main(String[] args) {
		String basePathStr = null;
		
		for(int i=0; i<args.length; ++i) {
			if(args[i].equals("-w")) {
				if(++i >= args.length)
					break;
				basePathStr = args[i];
			}
		}
		
		if(basePathStr == null) {
			System.out.println("Usage: java -cp mmat.jar me.hwei.mctool.TileEntiyRepaire -w <world path>");
			return;
		}
		
		File basePath = new File(basePathStr);
		Pattern filenamePatten = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mcr");
		
		try {
			File regionPath = new File(basePath, "region");
			System.out.println("Check chunks...");
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
							ListTag tileEntitiesTag = (ListTag)levelTag.getValue().get("TileEntities");
							List<Tag> tileEntityList = tileEntitiesTag.getValue();
							ByteArrayTag blocksTag = (ByteArrayTag)levelTag.getValue().get("Blocks");
							byte[] blockData = blocksTag.getValue();
							
							for(Tag tag : tileEntityList) {
								CompoundTag tileEntityTag = (CompoundTag)tag;
								String id = ((StringTag)tileEntityTag.getValue().get("id")).getValue();
								Integer tex = ((IntTag)tileEntityTag.getValue().get("x")).getValue();
								Integer tey = ((IntTag)tileEntityTag.getValue().get("y")).getValue();
								Integer tez = ((IntTag)tileEntityTag.getValue().get("z")).getValue();
								int blockID = 0xff & (int)blockData[tey + (tez & 0xf) * 128 + (tex & 0xf) * 128 * 16];
								
								if(id.equalsIgnoreCase("Furnace") && blockID != 61 && blockID != 62) {
									System.out.printf("%5d %5d %5d %5s %3d\n", tex, tey, tez, id, blockID);
								} else if(id.equalsIgnoreCase("Sign") && blockID != 63 && blockID != 68) {
									System.out.printf("%5d %5d %5d %5s %3d\n", tex, tey, tez, id, blockID);
								} else if(id.equalsIgnoreCase("MobSpawner") && blockID != 52) {
									System.out.printf("%5d %5d %5d %5s %3d\n", tex, tey, tez, id, blockID);
								} else if(id.equalsIgnoreCase("Chest") && blockID != 54) {
									System.out.printf("%5d %5d %5d %5s %3d\n", tex, tey, tez, id, blockID);
								} else if(id.equalsIgnoreCase("Music") && blockID != 25) {
									System.out.printf("%5d %5d %5d %5s %3d\n", tex, tey, tez, id, blockID);
								} else if(id.equalsIgnoreCase("Trap") && blockID != 23) {
									System.out.printf("%5d %5d %5d %5s %3d\n", tex, tey, tez, id, blockID);
								} else if(id.equalsIgnoreCase("RecordPlayer") && blockID != 84) {
									System.out.printf("%5d %5d %5d %5s %3d\n", tex, tey, tez, id, blockID);
								} else if(id.equalsIgnoreCase("Piston") && blockID != 29 && blockID != 33 && blockID != 34) {
									System.out.printf("%5d %5d %5d %5s %3d\n", tex, tey, tez, id, blockID);
								}
								
								
							}
							
							dis.close();
						}
					}
					rfile.close();
				}
			}
			
			RegionFileCache.clear();
			
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
