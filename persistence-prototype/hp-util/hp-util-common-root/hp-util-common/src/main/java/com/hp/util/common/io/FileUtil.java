/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * File utility methods.
 * 
 * @author Fabiel Zuniga
 */
public final class FileUtil {

    // To deal with files see java.nio.file.Files
    // It is preferable to use the new java.nio to implement methods in this class

    private FileUtil() {

    }

    /**
     * Loads a file from the resources dir using the current thread context class loader.
     * <p>
     * Usage example:
     * <p>
     *
     * <pre>
     * try (InputStream inputStream = FileUtil.loadResource(&quot;MyFile&quot;)) {
     *     // Use inputStream
     *     // inputStream is auto-closed by using try-with-resource
     * }
     * </pre>
     *
     * @param path relative path excluding "resources" path. For example, to load a file located at
     *            {@code .../mymodule/src/test/resources/mydir/file} use {@code mydir/file}
     * @return an input stream of file specified by the given path if it exists, {@code null} if the
     *         file does not exists
     */
    public static InputStream loadResource(Path path) {
        return loadResource(path, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Loads a file from the resources dir.
     * <p>
     * Usage example:
     * <p>
     *
     * <pre>
     * try (InputStream inputStream = FileUtil.loadResource(&quot;MyFile&quot;, myClassLoader)) {
     *     // Use inputStream
     *     // inputStream is auto-closed by using try-with-resource
     * }
     * </pre>
     *
     * @param path relative path excluding "resources" path. For example, to load a file located at
     *            {@code .../mymodule/src/test/resources/mydir/file} use {@code mydir/file}
     * @param classLoader class loader used to load the resource
     * @return an input stream of file specified by the given path if it exists, {@code null} if the
     *         file does not exists
     */
    public static InputStream loadResource(Path path, ClassLoader classLoader) {
        if (path == null) {
            throw new NullPointerException("path cannot be null");
        }

        if (classLoader == null) {
            throw new NullPointerException("classLoader cannot be null");
        }

        return classLoader.getResourceAsStream(path.toFile().getPath());
    }

    /**
     * Opens a file.
     * <p>
     * This method uses {@link Files#newInputStream(Path, java.nio.file.OpenOption...)} if
     * {@code path} is found, otherwise it uses {@link #loadResource(Path)} to look into the
     * resources.
     *
     * @param path the path to the file
     * @return an input stream
     * @throws IOException if an I/O error occurs creating the stream
     */
    public static InputStream open(Path path) throws IOException {
        if (Files.exists(path)) {
            return Files.newInputStream(path);
        }

        return loadResource(path);
    }

    /**
     * Read all the bytes from a file. The method ensures that the file is closed when all bytes
     * have been read or an I/O error, or other runtime exception, is thrown.
     * <p>
     * This method uses {@link Files#readAllBytes(Path)} if {@code path} is found, otherwise it uses
     * {@link #loadResource(Path)} to look into the resources.
     * <p>
     * Note that this method is intended for simple cases where it is convenient to read all bytes
     * into a byte array. It is not intended for reading in large files.
     *
     * @param path the path to the file
     * @return a byte array containing the bytes read from the file
     * @throws IOException if an I/O error occurs reading from the stream
     * @throws FileNotFoundException if the {@code path} is not found
     * @throws OutOfMemoryError if an array of the required size cannot be allocated, for example
     *             the file is larger that {@code 2GB}
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String) checkRead} method is
     *             invoked to check read access to the file.
     */
    public static byte[] readAllBytes(Path path) throws IOException, FileNotFoundException {
        if (path == null) {
            throw new NullPointerException("path cannot be null");
        }

        if (Files.exists(path)) {
            return Files.readAllBytes(path);
        }

        try (InputStream input = loadResource(path)) {
            if (input != null) {
                return IoUtil.read(input);
            }
        }

        throw new FileNotFoundException("File not found: " + path);
    }

    /**
     * Converts a path string, or a sequence of strings that when joined form a path string, to a
     * {@code Path}. This method is just a convenience of
     * {@link FileSystem#getPath(String, String...)} that uses the default file system:
     * {@link FileSystems#getDefault()}
     *
     * @param first the path string or initial part of the path string
     * @param more additional strings to be joined to form the path string
     * @return the resulting {@code Path}
     * @throws InvalidPathException If the path string cannot be converted
     */
    public static Path getPath(String first, String... more) {
        return FileSystems.getDefault().getPath(first, more);
    }

    /**
     * Converts a path string, or a sequence of strings that when joined form a path string, to a
     * {@code Path}. This method is just a convenience of
     * {@link FileSystem#getPath(String, String...)} that uses the default file system:
     * {@link FileSystems#getDefault()}
     *
     * @param parent parent
     * @param first the path string or initial part of the path string
     * @param more additional strings to be joined to form the path string
     * @return the resulting {@code Path}
     * @throws InvalidPathException If the path string cannot be converted
     */
    public static Path getPath(Path parent, String first, String... more) throws InvalidPathException {
        List<String> components = new ArrayList<String>();
        components.add(first);
        if (more != null) {
            components.addAll(Arrays.asList(more));
        }

        return FileSystems.getDefault().getPath(parent.toFile().getPath(), components.toArray(new String[0]));
    }

    /**
     * Creates a path. This method is just a convenience of
     * {@link FileSystem#getPath(String, String...)} that uses the default file system:
     * {@link FileSystems#getDefault()}
     *
     * @param parent parent
     * @param first the path
     * @param more additional paths to be joined to form the path
     * @return the resulting {@code Path}
     * @throws InvalidPathException If the path cannot be created
     */
    public static Path getPath(Path parent, Path first, Path... more) throws InvalidPathException {
        List<String> components = new ArrayList<String>();
        components.add(first.toFile().getPath());
        if (more != null) {
            for (Path path : more) {
                components.add(path.toFile().getPath());
            }
        }

        return FileSystems.getDefault().getPath(parent.toFile().getPath(), components.toArray(new String[0]));
    }

    /**
     * Gets the path for the temporal directory.
     * <p>
     * See {@link Files#createTempDirectory(Path, String, java.nio.file.attribute.FileAttribute...)}
     * and
     * {@link Files#createTempFile(Path, String, String, java.nio.file.attribute.FileAttribute...)}
     * for the preferred method when dealing with files.
     *
     * @return the path for the temporal directory.
     */
    public static Path getTempDirectory() {
        return FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Translates from an ancestor to a target ancestor.
     * <p>
     * The implementation of this method is based on {@link Path#resolve(Path)} and
     * {@link Path#relativize(Path)}. Example:
     * 
     * <pre>
     * Path targetDescendant = target.resolve(source.relativize(sourceDescendant));
     * </pre>
     * 
     * @param descendant path to translate
     * @param ancestor reference ancestor
     * @param translatedAncestor translated reference
     * @return a translated path where the ancestor's path has been replaced by the translated
     *         ancestor's path
     */
    public static Path translate(Path descendant, Path ancestor, Path translatedAncestor) {
        if (descendant == null) {
            throw new NullPointerException("descendant cannot be null");
        }

        if (ancestor == null) {
            throw new NullPointerException("ancestor cannot be null");
        }

        if (translatedAncestor == null) {
            throw new NullPointerException("translatedAncestor cannot be null");
        }

        if (!descendant.startsWith(ancestor)) {
            throw new IllegalArgumentException(ancestor + " is not an ancestor of " + descendant);
        }

        /*
        if (path.equals(ancestor)) {
            return translatedAncestor;
        }

        String strPath = path.toFile().getPath();
        String strAncestor = ancestor.toFile().getPath();
        String relativePath = strPath.substring(strPath.indexOf(strAncestor) + strAncestor.length() + 1);
        return FileUtil.getPath(translatedAncestor, relativePath);
        */
        return translatedAncestor.resolve(ancestor.relativize(descendant));
    }

    /**
     * Deletes a file or recursively deletes a directory.
     * 
     * @param path path to delete
     * @throws IOException if I/O errors occur while executing the operation
     */
    public static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        Files.walkFileTree(path, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException error) throws IOException {
                // This method is invoked if the file's attributes could not be read, the file is a
                // directory that could not be opened, and other reasons.
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException error) throws IOException {
                if (error == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
                throw error;
            }
        });
    }

    /**
     * Copies a file or recursively copies a directory. If the target file exists it will be
     * replaced.
     * 
     * @param source path to copy
     * @param target path to copy to
     * @param copyOption copy option
     * @throws IllegalArgumentException if {@code source} does not exist
     * @throws IOException if I/O errors occur while executing the operation
     */
    public static void copyRecursively(final Path source, final Path target, final StandardCopyOption copyOption)
            throws IllegalArgumentException, IOException {
        if (!Files.exists(source)) {
            throw new IllegalArgumentException(source + " does not exist");
        }

        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetPath = translate(dir, source, target);
                if (!Files.exists(targetPath)) {
                    Files.createDirectory(targetPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetPath = translate(file, source, target);
                Files.copy(file, targetPath, copyOption);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
