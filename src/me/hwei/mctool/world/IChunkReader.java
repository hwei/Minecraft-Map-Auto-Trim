package me.hwei.mctool.world;

import java.io.DataInputStream;

public interface IChunkReader {

	public IChunkData read(DataInputStream chunkDataInputStream);

}
