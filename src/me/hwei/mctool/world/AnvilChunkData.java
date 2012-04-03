package me.hwei.mctool.world;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.NbtIo;
import com.mojang.nbt.Tag;

public class AnvilChunkData implements IChunkData {
	public static IChunkReader ChunkReader() {
		return new IChunkReader() {
			@Override
			public IChunkData read(DataInputStream chunkDataInputStream) {
				AnvilChunkData chunkData = new AnvilChunkData();
				try {
					CompoundTag chunkTag = NbtIo.read(chunkDataInputStream);
					CompoundTag levelTag = chunkTag.getCompound("Level");
					ListTag<? extends Tag> sectionTags = levelTag.getList("Sections");
					ArrayList<byte[]> sectionBlocks = new ArrayList<byte[]>();
					ArrayList<byte[]> sectionAdds = new ArrayList<byte[]>();
					ArrayList<Integer> yBases = new ArrayList<Integer>();
					int maxYBase = -1;
					for(int i=0; i<sectionTags.size(); ++i) {
						CompoundTag sectionTag = (CompoundTag)sectionTags.get(i);
						int yBase = sectionTag.getByte("Y") & 0xff;
						yBases.add(yBase);
						maxYBase = Math.max(maxYBase, yBase);
						sectionBlocks.add(sectionTag.getByteArray("Blocks"));
						sectionAdds.add(sectionTag.getByteArray("Add"));
					}
					chunkData.blocks = new byte[maxYBase+1][];
					chunkData.add = new byte[maxYBase+1][];
					for(int i=0; i<yBases.size(); ++i) {
						chunkData.blocks[yBases.get(i)] = sectionBlocks.get(i);
						chunkData.add[yBases.get(i)] = sectionAdds.get(i);
					}
					chunkData.xPos = levelTag.getInt("xPos");
					chunkData.zPos = levelTag.getInt("zPos");
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				return chunkData;
			}
		};
	}
	
	protected byte[][] blocks;
	protected byte[][] add;
	protected int xPos;
	protected int zPos;
	protected static final int SECTION_SIZE = 16 * 16 * 16;

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
			this.end = blocks.length << 12;
			jumpEmpty();
		}
		
		public BlockIterator(int yStart, int yEnd) {
			yStart = Math.max(yStart, 0);
			yEnd = Math.min(yEnd, blocks.length << 4);
			this.pos = yStart << 8;
			this.end = yEnd << 8;
			jumpEmpty();
		}
		
		protected int pos;
		protected int end;
		
		@Override
		public boolean hasNext() {
			return pos < end;
		}

		@Override
		public Integer next() {
			int yBase = pos >> 12;
			int cubicPos = pos & 0xfff;
			int result = blocks[yBase][cubicPos] & 0xff;
			if(add[yBase] != null && add[yBase].length != 0) {
				int slot = cubicPos >> 1;
		        int part = cubicPos & 1;
		        if (part == 0) {
		            result += (add[yBase][slot] & 0xf) << 8;
		        } else {
		        	result += ((add[yBase][slot] >> 4) & 0xf) << 8;
		        }
			}
			++pos;
			if((pos & 0xfff) == 0) {
				jumpEmpty();
			}
			return result;
		}

		@Override
		public void remove() {
		}
		
		protected void jumpEmpty() {
			int yBase = pos >> 12;
			while(yBase < blocks.length && blocks[yBase] == null) {
				++yBase;
			}
			pos = (yBase << 12) | (pos & 0xfff);
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
