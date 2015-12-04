/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cluster;

import java.security.MessageDigest;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class encode the class name into MD5 code to serve as object identifier for serialization, hence instead
 * of pushing the full class name on each message we only push 2 long in each message.
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class AAAMD5Identifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(AAAMD5Identifier.class);
    private static MessageDigest md = null;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final WriteLock writeLock = lock.writeLock();

    private long md5Long1 = 0;
    private long md5Long2 = 0;

    private static final void init(){
        if (md == null) {
            try {
                writeLock.lock();
                if (md == null) {
                    try {
                        md = MessageDigest.getInstance("MD5");
                    } catch (Exception e) {
                        LOGGER.error("Failed to initialize with MD5 Digest",e);
                    }
                }
            } finally {
                writeLock.unlock();
            }
        }
    }

    private AAAMD5Identifier(long[] l, int offset) {
        this.md5Long1 = l[offset];
        this.md5Long2 = l[offset + 1];
    }

    private AAAMD5Identifier(long _a, long _b) {
        this.md5Long1 = _a;
        this.md5Long2 = _b;
    }

    private AAAMD5Identifier(byte encodedRecordKey[]) {

        byte by[] = null;

        try {
            writeLock.lock();
            md.update(encodedRecordKey);
            by = md.digest();
        } finally {
            writeLock.unlock();
        }

        md5Long1 = (md5Long1 << 8) + (by[0] & 0xff);
        md5Long1 = (md5Long1 << 8) + (by[1] & 0xff);
        md5Long1 = (md5Long1 << 8) + (by[2] & 0xff);
        md5Long1 = (md5Long1 << 8) + (by[3] & 0xff);
        md5Long1 = (md5Long1 << 8) + (by[4] & 0xff);
        md5Long1 = (md5Long1 << 8) + (by[5] & 0xff);
        md5Long1 = (md5Long1 << 8) + (by[6] & 0xff);
        md5Long1 = (md5Long1 << 8) + (by[7] & 0xff);

        md5Long2 = (md5Long2 << 8) + (by[8] & 0xff);
        md5Long2 = (md5Long2 << 8) + (by[9] & 0xff);
        md5Long2 = (md5Long2 << 8) + (by[10] & 0xff);
        md5Long2 = (md5Long2 << 8) + (by[11] & 0xff);
        md5Long2 = (md5Long2 << 8) + (by[12] & 0xff);
        md5Long2 = (md5Long2 << 8) + (by[13] & 0xff);
        md5Long2 = (md5Long2 << 8) + (by[14] & 0xff);
        md5Long2 = (md5Long2 << 8) + (by[15] & 0xff);

    }

    @Override
    public int hashCode() {
        return (int) (md5Long1 ^ (md5Long2 >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        AAAMD5Identifier other = (AAAMD5Identifier) obj;
        if (other.md5Long1 == md5Long1 && other.md5Long2 == md5Long2)
            return true;
        return false;
    }

    public long getMd5Long1() {
        return this.md5Long1;
    }

    public long getMd5Long2() {
        return this.md5Long2;
    }

    public long[] toLongArray() {
        return new long[] {md5Long1, md5Long2};
    }

    public static final AAAMD5Identifier createClassID(final String className) {
        return new AAAMD5Identifier(className.getBytes());
    }

    public static final AAAMD5Identifier createClassID(byte data[]) {
        return new AAAMD5Identifier(data);
    }

    public static AAAMD5Identifier createClassID(long a, long b) {
        return new AAAMD5Identifier(a, b);
    }
}