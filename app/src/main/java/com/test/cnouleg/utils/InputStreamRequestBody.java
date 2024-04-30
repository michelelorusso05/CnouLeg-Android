package com.test.cnouleg.utils;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

public class InputStreamRequestBody extends RequestBody {
    private final MediaType contentType;
    private final ContentResolver contentResolver;
    private final Uri uri;

    public InputStreamRequestBody(ContentResolver contentResolver, Uri uri) {
        if (uri == null) throw new NullPointerException("uri == null");
        this.contentResolver = contentResolver;
        this.uri = uri;
        String mime = contentResolver.getType(uri);
        if (mime == null) mime = "application/octect-stream";

        this.contentType = MediaType.parse(mime);
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() {
        return -1;
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        InputStream inputStream = contentResolver.openInputStream(uri);
        assert inputStream != null;

        sink.writeAll(Okio.source(inputStream));
        inputStream.close();
    }
}