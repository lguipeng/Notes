package com.evernote.client.conn.mobile;

import android.support.annotation.NonNull;

import com.evernote.thrift.transport.TTransport;
import com.evernote.thrift.transport.TTransportException;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import okio.BufferedSink;

/**
 * A {@link TTransport} implementation, which uses an {@link OkHttpClient} to read and write data
 * to the Evernote service.
 *
 * @author rwondratschek
 */
@SuppressWarnings("unused")
public class TAndroidTransport extends TTransport {

    private static final MediaType MEDIA_TYPE_THRIFT = MediaType.parse("application/x-thrift");

    private final OkHttpClient mHttpClient;
    private final ByteStore mByteStore;
    private final String mUrl;

    private InputStream mResponseBody;
    private Map<String, String> mHeaders;

    /**
     * @param httpClient The HTTP client.
     * @param byteStore Holds the data until it's POSTed with the HTTP client.
     * @param url The note store URL.
     */
    public TAndroidTransport(@NonNull OkHttpClient httpClient, @NonNull ByteStore byteStore, @NonNull String url) {
        this(httpClient, byteStore, url, null);
    }

    /**
     *
     * @param httpClient The HTTP client.
     * @param byteStore Holds the data until it's POSTed with the HTTP client.
     * @param url The note store URL.
     * @param headers Additional headers which are POSTed.
     */
    public TAndroidTransport(OkHttpClient httpClient, ByteStore byteStore, String url, Map<String, String> headers) {
        mHttpClient = httpClient;
        mByteStore = byteStore;
        mUrl = url;
        mHeaders = headers;
    }

    public void addHeader(String name, String value) {
        if (mHeaders == null) {
            mHeaders = new HashMap<>();
        }
        mHeaders.put(name, value);
    }

    public void addHeaders(Map<String, String> headers) {
        if (mHeaders == null) {
            mHeaders = new HashMap<>();
        }
        mHeaders.putAll(headers);
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void open() throws TTransportException {
        // do nothing
    }

    @Override
    public void write(byte[] buf, int off, int len) throws TTransportException {
        try {
            mByteStore.write(buf, off, len);
        } catch (IOException e) {
            throw new TTransportException(e);
        }
    }

    @Override
    public void flush() throws TTransportException {
        Util.closeQuietly(mResponseBody);
        mResponseBody = null;

        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                if (mHeaders != null && mHeaders.containsKey("Content-Type")) {
                    return MediaType.parse(mHeaders.get("Content-Type"));
                } else {
                    return MEDIA_TYPE_THRIFT;
                }
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                copy(mByteStore.getInputStream(), sink.outputStream());
            }
        };

        try {
            Request.Builder builder = new Request.Builder()
                    .url(mUrl)
                    .post(requestBody);

            if (mHeaders != null) {
                for (String name : mHeaders.keySet()) {
                    builder.header(name, mHeaders.get(name));
                }
            }

            Response response = mHttpClient.newCall(builder.build()).execute();

            if (response.code() != 200) {
                throw new TTransportException("HTTP Response code: " + response.code() + ", message " + response.message());
            }

            mResponseBody = response.body().byteStream();

        } catch (Exception e) {
            throw new TTransportException(e);

        } finally {
            try {
                mByteStore.reset();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public int read(byte[] buf, int off, int len) throws TTransportException {
        if (mResponseBody == null) {
            throw new TTransportException("Response buffer is empty, no request.");
        }

        try {
            int ret = mResponseBody.read(buf, off, len);
            if (ret == -1) {
                throw new TTransportException("No more data available.");
            }
            return ret;

        } catch (IOException e) {
            throw new TTransportException(e);
        }
    }

    @Override
    public void close() {
        Util.closeQuietly(mResponseBody);
        mResponseBody = null;
    }

    protected void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int read;

        while ((read = inputStream.read(buffer)) >= 0) {
            outputStream.write(buffer, 0, read);
        }
    }
}
