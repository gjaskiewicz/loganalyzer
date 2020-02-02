package com.creditsuisse.loganalyzer.util;

import java.io.IOException;

/**
 * Interface class for selectively reading file.
 */
public interface IPartialFileReader {
    /** advances pointer until specified char is met and returns result */
    String readUntilChar(char end, long maxIndex) throws IOException;
    
    /** moves pointer until specified char is met */
    boolean skipUntilChar(char end, long maxIndex) throws IOException;
    
    /** moves pointer by given offset */
    void skip(int bytesToSkip) throws IOException;
    
    /** moves pointer to specific position */
    void seek(long index) throws IOException;
    
    /** gets pointer location */
    long getFilePointer() throws IOException;
}
