/*
 * Copyright 2012 Evernote Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.evernote.client.conn.mobile;


import android.support.annotation.NonNull;

import com.squareup.okhttp.internal.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Holds all the data in memory until a threshold is reached. Then it writes all the data on disk.
 *
 * @author rwondratschek
 */
public class DiskBackedByteStore extends ByteStore {

    private static final int DEFAULT_MEMORY_BUFFER_SIZE = 2 * 1024 * 1024;

    /**
     * The maximum amount of memory to use before writing to disk.
     */
    protected final int mMaxMemory;
    protected final File mCacheDir;

    protected File mCacheFile;

    protected OutputStream mCurrentOutputStream;
    protected FileOutputStream mFileOutputStream;
    protected ByteArrayOutputStream mByteArrayOutputStream;

    protected int mBytesWritten;
    protected boolean mClosed;

    protected InputStream mInputStream;

    /**
     * @param cacheDir A directory where the temporary data is stored.
     * @param maxMemory The threshold before the data is written to disk.
     */
    protected DiskBackedByteStore(File cacheDir, int maxMemory) {
        mCacheDir = cacheDir;
        mMaxMemory = maxMemory;
    }

    @Override
    public void write(@NonNull byte[] buffer, int offset, int count) throws IOException {
        initBuffers();
        swapIfNecessary(count);

        mCurrentOutputStream.write(buffer, offset, count);
        mBytesWritten += count;
    }

    @Override
    public void write(int oneByte) throws IOException {
        initBuffers();
        swapIfNecessary(1);

        mCurrentOutputStream.write(oneByte);
        mBytesWritten++;
    }

    protected String generateRandomFileName() {
        return Math.random() * Long.MAX_VALUE + ".tft";
    }

    private void initBuffers() throws IOException {
        if (mClosed) {
            throw new IOException("Already closed");
        }
        if (mCurrentOutputStream != null) {
            return;
        }

        if (mByteArrayOutputStream == null) {
            mByteArrayOutputStream = new ByteArrayOutputStream(mMaxMemory);
        }
        if (mCurrentOutputStream == null) {
            mCurrentOutputStream = mByteArrayOutputStream;
        }
    }

    private void swapIfNecessary(int delta) throws IOException {
        if (isSwapRequired(delta)) {
            swapToDisk();
        }
    }

    private boolean isSwapRequired(int delta) {
        return !swapped() && mBytesWritten + delta > mMaxMemory;
    }

    private boolean swapped() {
        return mBytesWritten > mMaxMemory;
    }

    protected void swapToDisk() throws IOException {
        if (!mCacheDir.exists() && !mCacheDir.mkdirs()) {
            throw new IOException("could not create cache dir");
        }
        if (!mCacheDir.isDirectory()) {
            throw new IOException("cache dir is no directory");
        }

        mCacheFile = new File(mCacheDir, generateRandomFileName());
        mFileOutputStream = new FileOutputStream(mCacheFile);

        mByteArrayOutputStream.writeTo(mFileOutputStream);
        Util.closeQuietly(mByteArrayOutputStream);
        mByteArrayOutputStream = null;

        mCurrentOutputStream = mFileOutputStream;
    }

    @Override
    public void close() throws IOException {
        if (!mClosed) {
            Util.closeQuietly(mFileOutputStream);
            Util.closeQuietly(mByteArrayOutputStream);
            mClosed = true;
        }
    }

    @Override
    public int getBytesWritten() {
        return mBytesWritten;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (mInputStream != null) {
            return mInputStream;
        }

        close();

        if (mByteArrayOutputStream != null) {
            mInputStream = new ByteArrayInputStream(mByteArrayOutputStream.toByteArray());
        } else {
            mInputStream = new FileInputStream(mCacheFile);
        }

        return mInputStream;
    }

    @Override
    public void reset() throws IOException {
        try {
            close();
            Util.closeQuietly(mInputStream);

            if (mCacheFile != null && mCacheFile.isFile()) {
                if (!mCacheFile.delete()) {
                    throw new IOException("could not delete cache file");
                }
            }
        } finally {
            mByteArrayOutputStream = null;
            mFileOutputStream = null;
            mCurrentOutputStream = null;
            mInputStream = null;
            mBytesWritten = 0;
            mClosed = false;
        }
    }

    public static class Factory implements ByteStore.Factory {

        private final File mCacheDir;
        private final int mMaxMemory;

        /**
         * @param cacheDir A directory where the temporary data is stored.
         */
        public Factory(File cacheDir) {
            this(cacheDir, DEFAULT_MEMORY_BUFFER_SIZE);
        }

        /**
         * @param cacheDir A directory where the temporary data is stored.
         * @param maxMemory The threshold before the data is written to disk.
         */
        public Factory(File cacheDir, int maxMemory) {
            mCacheDir = cacheDir;
            mMaxMemory = maxMemory;
        }

        @Override
        public DiskBackedByteStore create() {
            return new DiskBackedByteStore(mCacheDir, mMaxMemory);
        }
    }
}
