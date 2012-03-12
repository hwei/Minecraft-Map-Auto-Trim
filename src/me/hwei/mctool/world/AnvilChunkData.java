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
	
	protected class BlockIterator implements Iterator<Integer> {
		
		protected int yBase;
		protected int pos;
		protected boolean hasNext;
		
		public BlockIterator() {
			yBase = 0;
			pos = 0;
			for(;yBase < blocks.length && blocks[yBase] == null; ++yBase) {
			}
			hasNext = (yBase != blocks.length);
		}
		
		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public Integer next() {
			int result = blocks[yBase][pos] & 0xff;
			if(add[yBase] != null && add[yBase].length != 0) {
				int slot = pos >> 1;
		        int part = pos & 1;
		        if (part == 0) {
		            result += (add[yBase][slot] & 0xf) << 8;
		        } else {
		        	result += ((add[yBase][slot] >> 4) & 0xf) << 8;
		        }
			}
			++pos;
			if(pos == SECTION_SIZE) {
				pos = 0;
				do
				{
					++yBase;
				}while(yBase < blocks.length &&
						(blocks[yBase] == null || blocks[yBase].length == 0));
				hasNext = (yBase != blocks.length);
			}
			
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
