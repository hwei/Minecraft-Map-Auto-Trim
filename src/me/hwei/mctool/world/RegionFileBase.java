package me.hwei.mctool.world;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class RegionFileBase implements Closeable {
	protected static final int SECTOR_BYTES = 4096;
	protected static final int SECTOR_INTS = SECTOR_BYTES / 4;

	protected static final int CHUNK_HEADER_SIZE = 5;

	protected final File fileName;
	protected RandomAccessFile file;
	protected final int offsets[];
	protected ArrayList<Boolean> sectorFree;
	
	public RegionFileBase(File path, String mode) {
		offsets = new int[SECTOR_INTS];
		this.fileName = path;
		//debugln("REGION LOAD " + fileName);
		try {
            file = new RandomAccessFile(path, mode);
            
            /* set up the available sector map */
            int nSectors = (int) file.length() / SECTOR_BYTES;
            sectorFree = new ArrayList<Boolean>(nSectors);

            for (int i = 0; i < nSectors; ++i) {
                sectorFree.add(true);
            }

            sectorFree.set(0, false); // chunk offset table
            sectorFree.set(1, false); // for the last modified info

            file.seek(0);
            for (int i = 0; i < SECTOR_INTS; ++i) {
                int offset = file.readInt();
                offsets[i] = offset;
                if (offset != 0 && (offset >> 8) + (offset & 0xFF) <= nSectors) {
                    for (int sectorNum = 0; sectorNum < (offset & 0xFF); ++sectorNum) {
                        sectorFree.set((offset >> 8) + sectorNum, false);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	/* is this an invalid chunk coordinate? */
    protected boolean outOfBounds(int x, int z) {
        return x < 0 || x >= 32 || z < 0 || z >= 32;
    }

    protected int getOffset(int x, int z) {
        return offsets[x + z * 32];
    }

    public boolean hasChunk(int x, int z) {
        return getOffset(x, z) != 0;
    }
    
    protected void setOffset(int x, int z, int offset) throws IOException {
        offsets[x + z * 32] = offset;
        file.seek((x + z * 32) * 4);
        file.writeInt(offset);
    }

    protected void setTimestamp(int x, int z, int value) throws IOException {
        file.seek(SECTOR_BYTES + (x + z * 32) * 4);
        file.writeInt(value);
    }

    @Override
    public void close() throws IOException {
        file.close();
        boolean isEmpty = true;
        for(int i=2; i<sectorFree.size(); ++i) {
        	if(sectorFree.get(i) == false) {
        		isEmpty = false;
        		break;
        	}
        }
        if(isEmpty) {
        	this.fileName.deleteOnExit();
        	debugln("Delete file: " + this.fileName.getName());
        }
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
	
    // various small debug printing helpers
	protected void debug(String in) {
        System.out.print(in);
    }

	protected void debugln(String in) {
        debug(in + "\n");
    }

	protected void debug(String mode, int x, int z, String in) {
        debug("REGION " + mode + " " + fileName.getName() + "[" + x + "," + z + "] = " + in);
    }

	protected void debugln(String mode, int x, int z, String in) {
        debug(mode, x, z, in + "\n");
    }
}
