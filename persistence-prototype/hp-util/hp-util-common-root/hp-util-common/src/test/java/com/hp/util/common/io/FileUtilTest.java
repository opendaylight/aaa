/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class FileUtilTest {

    @Test
    public void testLoadResourceFile() throws IOException {
        try (InputStream inputStream = FileUtil.loadResource(FileUtil.getPath("test-file"))) {
            Assert.assertNotNull(inputStream);
            Assert.assertTrue(inputStream.read(new byte[5]) > 0);
        }

        Assert.assertNull(FileUtil.loadResource(FileUtil.getPath("nonexistent")));
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidLoadResourceFile() throws IOException {
        Path invalidPath = null;
        try (InputStream inputStream = FileUtil.loadResource(invalidPath)) {
        }
    }

    @Test
    public void testOpen() throws IOException {
        try (InputStream inputStream = FileUtil.open(FileUtil.getPath("test-file"))) {
            Assert.assertNotNull(inputStream);
            Assert.assertTrue(inputStream.read(new byte[5]) > 0);
        }

        try (InputStream inputStream = FileUtil.open(FileUtil.getPath("test-file-outside-resources"))) {
            Assert.assertNotNull(inputStream);
            Assert.assertTrue(inputStream.read(new byte[5]) > 0);
        }

        Assert.assertNull(FileUtil.open(FileUtil.getPath("nonexistent")));
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidOpen() throws IOException {
        Path invalidPath = null;
        try (InputStream inputStream = FileUtil.open(invalidPath)) {
        }
    }

    @Test
    public void testReadAllBytes() throws IOException {
        byte[] content = FileUtil.readAllBytes(FileUtil.getPath("test-file"));
        Assert.assertNotNull(content);
        Assert.assertEquals(9, content.length);
    }

    @Test
    public void testInvalidReadAllBytes() {
        final Path invalidPathNull = null;
        final Path invalidPathNonExistent = FileUtil.getPath("NonExistentFile");

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                FileUtil.readAllBytes(invalidPathNull);
            }
        });

        ThrowableTester.testThrows(FileNotFoundException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                FileUtil.readAllBytes(invalidPathNonExistent);
            }
        });
    }

    @Test
    public void testGetPath() {
        Path path = FileUtil.getPath("file");
        Assert.assertNotNull(path);
        Assert.assertEquals("file", path.toFile().getName());

        path = FileUtil.getPath("parent", "file");
        Assert.assertNotNull(path);
        Assert.assertEquals("parent" + File.separator + "file", path.toFile().getPath());
    }

    @Test
    public void testGetPathWithParent() {
        Path parent = FileUtil.getPath("parent");
        Path path = FileUtil.getPath(parent, "file");
        Assert.assertNotNull(path);
        Assert.assertEquals("parent" + File.separator + "file", path.toFile().getPath());

        path = FileUtil.getPath(parent, "parent2", "file");
        Assert.assertNotNull(path);
        Assert.assertEquals("parent" + File.separator + "parent2" + File.separator + "file", path.toFile().getPath());
    }

    @Test
    public void testGetPathWithParentUsingPaths() {
        Path parent = FileUtil.getPath("parent");
        Path child = FileUtil.getPath("child");
        Path path = FileUtil.getPath(parent, child);
        Assert.assertNotNull(path);
        Assert.assertEquals("parent" + File.separator + "child", path.toFile().getPath());

        Path grandchild = FileUtil.getPath("grandchild");
        path = FileUtil.getPath(parent, child, grandchild);
        Assert.assertEquals("parent" + File.separator + "child" + File.separator + "grandchild", path.toFile()
                .getPath());
    }

    @Test
    public void testGetTempDirectory() {
        Path path = FileUtil.getTempDirectory();
        Assert.assertNotNull(path);
        Assert.assertTrue(Files.exists(path));
    }

    @Test
    public void testTranslate() {
        Path ancestor = FileUtil.getPath("ancestor1", "ancestor2");
        Path path = FileUtil.getPath(ancestor, "parent", "file");
        Path translatedAncestor = FileUtil.getPath("translated");

        Path translatedPath = FileUtil.translate(path, ancestor, translatedAncestor);
        Assert.assertNotNull(translatedPath);
        Assert.assertEquals("translated" + File.separator + "parent" + File.separator + "file", translatedPath.toFile()
                .getPath());
    }

    @Test
    public void testAncestorTranslation() {
        Path ancestor = FileUtil.getPath("ancestor1", "ancestor2");
        Path path = ancestor;
        Path translatedAncestor = FileUtil.getPath("translated");

        Path translatedPath = FileUtil.translate(path, ancestor, translatedAncestor);

        Assert.assertNotNull(translatedPath);
        Assert.assertEquals(translatedAncestor, translatedPath);
    }

    @Test
    public void testInvalidTranslate() {
        final Path validAncestor = FileUtil.getPath("ancestor1", "ancestor2");
        final Path invalidAncestor = null;

        final Path validTranslatedAncestor = FileUtil.getPath("translated");
        final Path invalidTranslatedAncestor = null;

        final Path validPath = FileUtil.getPath(validAncestor, "parent", "file");
        final Path invalidPath = null;
        final Path invalidPathByAncestor = FileUtil.getPath("other_ancestor", "file");

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                FileUtil.translate(invalidPath, validAncestor, validTranslatedAncestor);
            }
        });

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                FileUtil.translate(validPath, invalidAncestor, validTranslatedAncestor);
            }
        });

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                FileUtil.translate(validPath, validAncestor, invalidTranslatedAncestor);
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                FileUtil.translate(invalidPathByAncestor, validAncestor, validTranslatedAncestor);
            }
        });
    }

    @Test
    public void testDeleteRecursivelyFile() throws IOException {
        Path filePath = FileUtil.getPath(FileUtil.getTempDirectory(), "test-file-to-delete-" + UUID.randomUUID());

        try (OutputStream output = Files.newOutputStream(filePath)) {
            byte[] data = new byte[] { (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5 };
            output.write(data);
        }

        Assert.assertTrue(Files.exists(filePath));

        FileUtil.deleteRecursively(filePath);

        Assert.assertFalse(Files.exists(filePath));
    }

    @Test
    public void testDeleteRecursivelyFolder() throws IOException {
        Path folderPath = FileUtil.getPath(FileUtil.getTempDirectory(), "test-folder-to-delete-" + UUID.randomUUID());
        Path filePath = FileUtil.getPath(folderPath, "test-file-to-delete");

        Files.createDirectory(folderPath);

        try (OutputStream output = Files.newOutputStream(filePath)) {
            byte[] data = new byte[] { (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5 };
            output.write(data);
        }

        Assert.assertTrue(Files.exists(folderPath));
        Assert.assertTrue(Files.exists(filePath));

        FileUtil.deleteRecursively(folderPath);

        Assert.assertFalse(Files.exists(folderPath));
        Assert.assertFalse(Files.exists(filePath));
    }

    @Test
    public void testCopyRecursivelyFile() throws IOException {
        Path filePath = FileUtil.getPath(FileUtil.getTempDirectory(), "test-file-to-copy-" + UUID.randomUUID());
        Path copyFilePath = FileUtil.getPath(FileUtil.getTempDirectory(), "test-file-copy-" + UUID.randomUUID());

        byte[] expectedFileContent = new byte[] { (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5 };

        try {
            try (OutputStream output = Files.newOutputStream(filePath)) {
                output.write(expectedFileContent);
            }

            Assert.assertTrue(Files.exists(filePath));
            Assert.assertFalse(Files.exists(copyFilePath));

            FileUtil.copyRecursively(filePath, copyFilePath, StandardCopyOption.COPY_ATTRIBUTES);

            Assert.assertTrue(Files.exists(copyFilePath));

            byte[] actualFileContent = null;
            try (InputStream input = Files.newInputStream(copyFilePath)) {
                actualFileContent = IoUtil.read(input);
            }

            Assert.assertArrayEquals(expectedFileContent, actualFileContent);
        }
        finally {
            FileUtil.deleteRecursively(filePath);
            FileUtil.deleteRecursively(copyFilePath);
        }
    }

    @Test
    public void testCopyRecursivelyFolder() throws IOException {
        Path folderPath = FileUtil.getPath(FileUtil.getTempDirectory(), "test-folder-to-copy-" + UUID.randomUUID());
        Path childFilePath = FileUtil.getPath(folderPath, "test-child-file-to-copy");
        Path childFolderPath = FileUtil.getPath(folderPath, "test-child-folder-to-copy");
        Path grandchildFilePath = FileUtil
.getPath(childFolderPath, "test-grandchild-file-to-copy");

        Path copyFolderPath = FileUtil.getPath(FileUtil.getTempDirectory(), "test-folder-copy-" + UUID.randomUUID());

        byte[] expectedChildFileContent = new byte[] { (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5 };
        byte[] expectedGranchildFileContent = new byte[] { (byte) 5, (byte) 4, (byte) 3, (byte) 2, (byte) 1, (byte) 0 };

        try {
            Files.createDirectory(folderPath);
            Files.createDirectory(childFolderPath);

            try (OutputStream output = Files.newOutputStream(childFilePath)) {
                output.write(expectedChildFileContent);
            }

            try (OutputStream output = Files.newOutputStream(grandchildFilePath)) {
                output.write(expectedGranchildFileContent);
            }

            Assert.assertTrue(Files.exists(childFilePath));
            Assert.assertTrue(Files.exists(grandchildFilePath));
            Assert.assertFalse(Files.exists(copyFolderPath));

            FileUtil.copyRecursively(folderPath, copyFolderPath, StandardCopyOption.COPY_ATTRIBUTES);

            Assert.assertTrue(Files.exists(copyFolderPath));

            byte[] actualChildFileContent = null;
            try (InputStream input = Files
                    .newInputStream(FileUtil.translate(childFilePath, folderPath, copyFolderPath))) {
                actualChildFileContent = IoUtil.read(input);
            }
            Assert.assertArrayEquals(expectedChildFileContent, actualChildFileContent);

            byte[] actualGranchildFileContent = null;
            try (InputStream input = Files.newInputStream(FileUtil.translate(grandchildFilePath, folderPath,
                    copyFolderPath))) {
                actualGranchildFileContent = IoUtil.read(input);
            }
            Assert.assertArrayEquals(expectedGranchildFileContent, actualGranchildFileContent);
        }
        finally {
            FileUtil.deleteRecursively(folderPath);
            FileUtil.deleteRecursively(copyFolderPath);
        }
    }
}
