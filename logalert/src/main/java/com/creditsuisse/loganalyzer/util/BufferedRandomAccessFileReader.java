package com.creditsuisse.loganalyzer.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * Helper class for handling random access file.
 */
public class BufferedRandomAccessFileReader implements IPartialFileReader {

    private RandomAccessFile raf;

    public BufferedRandomAccessFileReader(RandomAccessFile raf) {
        this.raf = raf;
    }
    
    public String readUntilChar(char end, long maxIndex) throws IOException {
        if (raf.getFilePointer() >= maxIndex) {
            return null;
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte c = ' ';
        do {
            // might be better to bulk-read byte array
            c = raf.readByte();
            out.write(c);
            
            if (c == end) {
                return out.toString(StandardCharsets.UTF_8.name());
            }
            
            if (raf.getFilePointer() >= maxIndex) {
                return null;
            }
        } while (c != end);
        
        return out.toString(StandardCharsets.UTF_8.name());
    }

    public boolean skipUntilChar(char end, long maxIndex) throws IOException {
        if (raf.getFilePointer() >= maxIndex) {
            return false;
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte c = ' ';
        do {
            
            // might be better to bulk-read byte array
            c = raf.readByte();
            out.write(c);
            
            if (c == end) {
                return true;
            }
            
            if (raf.getFilePointer() >= maxIndex) {
                return false;
            }
        } while (c != end);
        
        return true;
    }

    public void skip(int i) throws IOException {
        raf.seek(raf.getFilePointer() + i);
    }

    public void seek(long index) throws IOException {
        raf.seek(index);
    }

    public long getFilePointer() throws IOException {
        return raf.getFilePointer();
    }
}
