/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Input/Output utility methods.
 * 
 * @author Fabiel Zuniga
 */
public final class IoUtil {

    private static final int BUFFER_SIZE = 1024;

    private IoUtil() {

    }

    // Note: Do not close streams that were externally created.

    // InputStream.read(byte[]) method blocks until input data is available, end of file is
    // detected, or an exception is thrown.

    /**
     * Reads the content of the given input stream until end of file is detected (blocking call).
     * <p>
     * See {@link Files#readAllBytes(Path)} for the preferred method when dealing with files.
     *
     * @param input input stream to read. This stream will remain opened after the invocation of
     *            this method.
     * @return the content
     * @throws IOException if errors occur while reading
     */
    public static byte[] read(InputStream input) throws IOException {
        return read(input, new byte[BUFFER_SIZE]);
    }

    /**
     * Reads the content of the given input stream until end of file is detected (blocking call).
     * <p>
     * See {@link Files#readAllBytes(Path)} for the preferred method when dealing with files.
     *
     * @param input input stream to read. This stream will remain opened after the invocation of
     *            this method.
     * @param buffer storage used to temporarily store data while it is being moved from one place
     *            to another. This is convenient parameter to enhance performance in cases where big
     *            buffers are already reserved to assist read operations.
     * @return the content
     * @throws IOException if errors occur while reading
     */
    public static byte[] read(InputStream input, byte[] buffer) throws IOException {
        if (buffer == null) {
            throw new NullPointerException("buffer cannot be null");
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            int bytesRead;
            while ((bytesRead = input.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * Reads the content of the given input stream until end of file is detected or {@code length}
     * bytes are read. Useful for reading bytes from an input stream that produces data (Like a
     * socket). so the process blocks until the expected number of bytes is produced.
     * <p>
     * See {@link Files#readAllBytes(Path)} for the preferred method when dealing with files.
     *
     * @param input input stream to read. This stream will remain opened after the invocation of
     *            this method.
     * @param length maximum number of bytes to read
     * @return the content
     * @throws IOException if errors occur while reading
     */
    public static byte[] read(InputStream input, int length) throws IOException {
        return read(input, length, new byte[BUFFER_SIZE]);
    }

    /**
     * Reads the content of the given input stream until end of file is detected or {@code length}
     * bytes are read. Useful for reading bytes from an input stream that produces data (Like a
     * socket). so the process blocks until the expected number of bytes is produced.
     * <p>
     * See {@link Files#readAllBytes(Path)} for the preferred method when dealing with files.
     *
     * @param input input stream to read. This stream will remain opened after the invocation of
     *            this method.
     * @param length maximum number of bytes to read
     * @param buffer storage used to temporarily store data while it is being moved from one place
     *            to another. This is convenient parameter to enhance performance in cases where big
     *            buffers are already reserved to assist read operations.
     * @return the content
     * @throws IOException if errors occur while reading
     */
    public static byte[] read(InputStream input, int length, byte[] buffer) throws IOException {
        if (buffer == null) {
            throw new NullPointerException("buffer cannot be null");
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            long totalBytes = 0;
            int bytesToRead = Math.min(buffer.length, length);
            int bytesRead;
            while (bytesToRead > 0 && (bytesRead = input.read(buffer, 0, bytesToRead)) > 0) {
                totalBytes += bytesRead;
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                bytesToRead = (int) Math.min(buffer.length, length - totalBytes);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * Copies the content of the given input stream to the given output stream until end of file is
     * detected (blocking call).
     * <p>
     * See {@link Files#copy(Path, Path, java.nio.file.CopyOption...)} for the preferred method when
     * dealing with files.
     *
     * @param input input stream. This stream will remain opened after the invocation of this
     *            method.
     * @param output output stream. This stream will remain opened after the invocation of this
     *            method.
     * @return number of copied bytes
     * @throws IOException if errors occur while reading
     */
    public static long copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, new byte[BUFFER_SIZE]);
    }

    /**
     * Copies the content of the given input stream to the given output stream until end of file is
     * detected (blocking call).
     * <p>
     * See {@link Files#copy(Path, Path, java.nio.file.CopyOption...)} for the preferred method when
     * dealing with files.
     *
     * @param input input stream. This stream will remain opened after the invocation of this
     *            method.
     * @param output output stream. This stream will remain opened after the invocation of this
     *            method.
     * @param buffer storage used to temporarily store data while it is being moved from one place
     *            to another. This is convenient parameter to enhance performance in cases where big
     *            buffers are already reserved to assist read operations.
     * @return number of copied bytes
     * @throws IOException if errors occur while reading
     */
    public static long copy(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        if (buffer == null) {
            throw new NullPointerException("buffer cannot be null");
        }

        long totalBytes = 0;
        int bytesRead;
        while ((bytesRead = input.read(buffer)) > 0) {
            output.write(buffer, 0, bytesRead);
            totalBytes += bytesRead;
        }
        return totalBytes;
    }

    /**
     * Copies the content of the given input stream to the given output stream until end of file is
     * detected or {@code length} bytes are read. Useful for reading bytes from an input stream that
     * produces data (Like a socket) so the process blocks until the expected number of bytes is
     * produced.
     * <p>
     * See {@link Files#copy(Path, Path, java.nio.file.CopyOption...)} for the preferred method when
     * dealing with files.
     *
     * @param input input stream. This stream will remain opened after the invocation of this
     *            method.
     * @param output output stream. This stream will remain opened after the invocation of this
     *            method.
     * @param length maximum number of bytes to read
     * @return number of copied bytes
     * @throws IOException if errors occur while reading
     */
    public static long copy(InputStream input, OutputStream output, long length) throws IOException {
        return copy(input, output, length, new byte[BUFFER_SIZE]);
    }

    /**
     * Copies the content of the given input stream to the given output stream until end of file is
     * detected or {@code length} bytes are read. Useful for reading bytes from an input stream that
     * produces data (Like a socket) so the process blocks until the expected number of bytes is
     * produced.
     * <p>
     * See {@link Files#copy(Path, Path, java.nio.file.CopyOption...)} for the preferred method when
     * dealing with files.
     *
     * @param input input stream. This stream will remain opened after the invocation of this
     *            method.
     * @param output output stream. This stream will remain opened after the invocation of this
     *            method.
     * @param length maximum number of bytes to read
     * @param buffer storage used to temporarily store data while it is being moved from one place
     *            to another. This is convenient parameter to enhance performance in cases where big
     *            buffers are already reserved to assist read operations.
     * @return number of copied bytes
     * @throws IOException if errors occur while reading
     */
    public static long copy(InputStream input, OutputStream output, long length, byte[] buffer) throws IOException {
        if (buffer == null) {
            throw new NullPointerException("buffer cannot be null");
        }

        long totalBytes = 0;
        int bytesToRead = (int) Math.min(buffer.length, length);
        int bytesRead;
        while (bytesToRead > 0 && (bytesRead = input.read(buffer, 0, bytesToRead)) > 0) {
            totalBytes += bytesRead;
            output.write(buffer, 0, bytesRead);
            bytesToRead = (int) Math.min(buffer.length, length - totalBytes);
        }
        return totalBytes;
    }
}
