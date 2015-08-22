package com.evernote.client.conn.mobile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Holds the data until it's written to the server.
 *
 * @author rwondratschek
 * @see MemoryByteStore
 * @see DiskBackedByteStore
 */
public abstract class ByteStore extends OutputStream {

    /**
     * @return The number of bytes which this instance is holding at the moment.
     */
    public abstract int getBytesWritten();

    /**
     * @return An {@link InputStream} to read the data from this instance.
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Reset all pointers.
     */
    public abstract void reset() throws IOException;

    /**
     * A factory to create a byte store.
     */
    public interface Factory {
        /**
         * @return A new instance.
         */
        ByteStore create();
    }
}
