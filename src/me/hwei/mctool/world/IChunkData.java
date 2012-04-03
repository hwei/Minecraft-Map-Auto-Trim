package me.hwei.mctool.world;

import java.util.Iterator;

public interface IChunkData {
	public Iterator<Integer> blockIterator();
	public Iterator<Integer> blockIterator(int yStart, int yEnd);
	public int getXPos();
	public int getZPos();
}
