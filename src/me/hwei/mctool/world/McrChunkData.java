package me.hwei.mctool.world;

import java.io.DataInputStream;
import java.util.Iterator;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.NbtIo;

public class McrChunkData implements IChunkData {
	public static IChunkReader ChunkReader() {
		return new IChunkReader() {
			@Override
			public IChunkData read(DataInputStream chunkDataInputStream) {
				McrChunkData chunkData = new McrChunkData();
				try {
					CompoundTag chunkTag = NbtIo.read(chunkDataInputStream);
					CompoundTag levelTag = chunkTag.getCompound("Level");
					chunkData.blocks = levelTag.getByteArray("Blocks");
					chunkData.xPos = levelTag.getInt("xPos");
					chunkData.zPos = levelTag.getInt("zPos");
				} catch (Exception e) {
					return null;
				}
				return chunkData;
			}
		};
	}
	
	protected byte[] blocks;
	protected int xPos;
	protected int zPos;
	protected static final int CHUNK_SIZE = 16 * 16 * 128;

	@Override
	public Iterator<Integer> blockIterator() {
		return new BlockIterator();
	}
	
	@Override
	public Iterator<Integer> blockIterator(int yStart, int yEnd) {
		return new BlockIterator(yStart, yEnd);
	}
	
	protected class BlockIterator implements Iterator<Integer> {
		
		public BlockIterator() {
			this.pos = 0;
			this.end = CHUNK_SIZE;
		}
		
		public BlockIterator(int yStart, int yEnd) {
			yStart = Math.max(yStart, 0);
			yEnd = Math.min(yEnd, 128);
			this.pos = yStart << 8;
			this.end = yEnd << 8;
		}
		
		protected int pos;
		protected int end;
		
		@Override
		public boolean hasNext() {
			return pos < end;
		}

		@Override
		public Integer next() {
			int mcrPos = (pos >> 8) | ((pos & 0xff) << 7);
			int result = blocks[mcrPos] & 0xff;
			++pos;
			return result;
		}

		@Override
		public void remove() {
		}
		
	}

	@Override
	public int getXPos() {
		return this.xPos;
	}

	@Override
	public int getZPos() {
		return this.zPos;
	}

	
}
