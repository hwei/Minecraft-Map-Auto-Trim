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
	
	protected class BlockIterator implements Iterator<Integer> {
		
		protected int pos = 0;
		
		@Override
		public boolean hasNext() {
			return pos < CHUNK_SIZE;
		}

		@Override
		public Integer next() {
			int result = blocks[pos] & 0xff;
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
