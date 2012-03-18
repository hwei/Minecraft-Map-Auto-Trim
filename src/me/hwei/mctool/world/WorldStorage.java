package me.hwei.mctool.world;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.NbtIo;

public class WorldStorage implements Closeable {

	protected static final int MCREGION_VERSION_ID = 0x4abc;
    protected static final int ANVIL_VERSION_ID = 0x4abd;
    
	public static WorldStorage load(File mapFolder) {
		File dataFile = new File(mapFolder, "level.dat");
		if(!dataFile.exists()) {
			dataFile = new File(mapFolder, "level.dat_old");
			if(!dataFile.exists())
				return null;
		}
		try {
            CompoundTag root = NbtIo.readCompressed(new FileInputStream(dataFile));
            CompoundTag data = root.getCompound("Data");
            if(data == null)
            	return null;
            File regionFolder = new File(mapFolder, "region");
            ArrayList<File> regionFiles = null;
            WorldCache worldCache = null;
            int ver = data.getInt("version");
            if(ver == MCREGION_VERSION_ID) {
            	regionFiles = getRegionFiles(regionFolder, ".mcr");
            	worldCache = new WorldCache(McrChunkData.ChunkReader());
            } else if(ver == ANVIL_VERSION_ID) {
            	regionFiles = getRegionFiles(regionFolder, ".mca");
            	worldCache = new WorldCache(AnvilChunkData.ChunkReader());
            }
            return new WorldStorage(data.getString("LevelName"), regionFiles, worldCache);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return null;
	}
	
	private static ArrayList<File> getRegionFiles(File regionFolder, String extName) {
        File[] list = regionFolder.listFiles(new FilenameExtFilter(extName));
        if (list != null) {
        	ArrayList<File> regionFiles = new ArrayList<File>();
            for (File file : list) {
                regionFiles.add(file);
            }
            return regionFiles;
        }
        return null;
    }
	
	private static class FilenameExtFilter implements FilenameFilter {
		public FilenameExtFilter(String ext) {
			this.ext = ext;
		}
		protected String ext;
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(ext);
		}
	}

	protected ArrayList<File> regionFiles;
	protected WorldCache worldCache;
	protected String levelName;
	
	protected WorldStorage(String levelName, ArrayList<File> regionFiles, WorldCache worldCache) {
		this.levelName = levelName;
		this.regionFiles = regionFiles;
		this.worldCache = worldCache;
	}
	
	public String getLevelName() {
		return this.levelName;
	}
	
	public ArrayList<ChunkHandle> getAllChunks() {
		ArrayList<ChunkHandle> chunks = new ArrayList<ChunkHandle>();
		for(int i=0; i<this.regionFiles.size(); ++i) {
			File regionFile = this.regionFiles.get(i);
			RegionFileForRead r = this.worldCache.getRegionFileForRead(regionFile);
			for(int rx=0; rx<32; ++rx) {
				for(int rz=0; rz<32; ++rz) {
					if(r.hasChunk(rx, rz)) {
						chunks.add(new ChunkHandle(rx, rz, regionFile, this.worldCache));
					}
				}
			}
		}
		return chunks;
	}
	
	@Override
	public void close() {
		this.worldCache.clearRegionCache();
	}
	
	@Override
	protected void finalize() throws Throwable {
	    try {
	        close();
	    } catch(Exception e) { 
	    }
	    
	    finally {
	        super.finalize();
	    }
	}
}
