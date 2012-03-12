package me.hwei.mctool.world;

import java.util.Iterator;

public interface IChunkData {
	public Iterator<Integer> blockIterator();
	public int getXPos();
	public int getZPos();
}
