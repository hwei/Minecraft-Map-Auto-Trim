package me.hwei.mctool.world;

import java.io.File;
import java.util.Iterator;

public class ChunkHandle {
	
	public ChunkHandle(int rx, int rz, File regionFile, WorldCache worldCache) {
		this.rx = rx;
		this.rz = rz;
		this.regionFile = regionFile;
		this.worldCache = worldCache;
		this.hasChunkPos = false;
	}
	
	public int getChunkX() {
		initChunkPos();
		return this.cx;
	}
	
	public int getChunkZ() {
		initChunkPos();
		return this.cz;
	}
	
	public void delete() {
		worldCache.deleteChunk(this);
	}

	public boolean hasAnyBlock(boolean[] blockIdMask) {
		IChunkData chunkData = worldCache.getChunkData(this);
		if(chunkData == null)
			return false;
		Iterator<Integer> iter = chunkData.blockIterator();
		while(iter.hasNext()) {
			int blockId = iter.next();
			if(blockIdMask[blockId]) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasAnyBlock(boolean[] blockIdMask, int yBegin, int yEnd) {
		IChunkData chunkData = worldCache.getChunkData(this);
		if(chunkData == null)
			return false;
		Iterator<Integer> iter = chunkData.blockIterator(yBegin, yEnd);
		while(iter.hasNext()) {
			int blockId = iter.next();
			if(blockIdMask[blockId]) {
				return true;
			}
		}
		return false;
	}
	
	protected IChunkData getChunkData() {
		IChunkData chunkData = worldCache.getChunkData(this);
		initChunkPos(chunkData);
		return chunkData;
	}
	
	protected void initChunkPos() {
		if(hasChunkPos)
			return;
		getChunkData();
	}
	
	protected void initChunkPos(IChunkData chunkData) {
		if(hasChunkPos)
			return;
		cx = chunkData.getXPos();
		cz = chunkData.getZPos();
		hasChunkPos = true;
	}
	
	protected boolean hasChunkPos;
	protected int cx;
	protected int cz;
	protected int rx;
	protected int rz;
	protected File regionFile;
	protected WorldCache worldCache;
}
