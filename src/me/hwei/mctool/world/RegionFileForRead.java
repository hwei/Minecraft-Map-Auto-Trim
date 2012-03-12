package me.hwei.mctool.world;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class RegionFileForRead extends RegionFileBase {

    private static final int VERSION_GZIP = 1;
    private static final int VERSION_DEFLATE = 2;

    public RegionFileForRead(File path) {
        super(path, "r");
    }

    /*
     * gets an (uncompressed) stream representing the chunk data returns null if
     * the chunk is not found or an error occurs
     */
    public synchronized DataInputStream getChunkDataInputStream(int x, int z) {
        if (outOfBounds(x, z)) {
            debugln("READ", x, z, "out of bounds");
            return null;
        }

        try {
            int offset = getOffset(x, z);
            if (offset == 0) {
                return null;
            }

            int sectorNumber = offset >> 8;
            int numSectors = offset & 0xFF;

            if (sectorNumber + numSectors > sectorFree.size()) {
                debugln("READ", x, z, "invalid sector");
                return null;
            }

            file.seek(sectorNumber * SECTOR_BYTES);
            int length = file.readInt();

            if (length > SECTOR_BYTES * numSectors) {
                debugln("READ", x, z, "invalid length: " + length + " > 4096 * " + numSectors);
                return null;
            }

            byte version = file.readByte();
            if (version == VERSION_GZIP) {
                byte[] data = new byte[length - 1];
                file.read(data);
                DataInputStream ret = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(data))));
                // debug("READ", x, z, " = found");
                return ret;
            } else if (version == VERSION_DEFLATE) {
                byte[] data = new byte[length - 1];
                file.read(data);
                DataInputStream ret = new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(data))));
                // debug("READ", x, z, " = found");
                return ret;
            }

            debugln("READ", x, z, "unknown version " + version);
            return null;
        } catch (IOException e) {
            debugln("READ", x, z, "exception");
            return null;
        }
    }
}