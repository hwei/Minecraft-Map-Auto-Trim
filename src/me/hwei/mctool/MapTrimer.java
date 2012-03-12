package me.hwei.mctool;

import java.io.File;

public class MapTrimer {

	public static MapTrimer create(File mapFolder, int dilationSize,
			boolean[] preservedIds, int[] rect) {
		
		
		
		return null;
	}
	
	protected int dilationSize;
	protected boolean[] preservedIds;
	protected int[] rect;
	protected MapTrimer(int dilationSize, boolean[] preservedIds, int[] rect) {
		this.dilationSize = dilationSize;
		this.preservedIds = preservedIds;
		this.rect = rect;
	}
}
