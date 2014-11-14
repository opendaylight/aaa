package org.opendaylight.aaa.idm.persistence;

import java.security.MessageDigest;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class MD5Calculator {

	private static MessageDigest md = null;
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static WriteLock writeLock = lock.writeLock();
    private static final byte[] SOLT = new byte[]{(byte)1,(byte)88,(byte)4,(byte)221,(byte)2,(byte)72};
    
    public static String getMD5(byte data[]) {
    	byte temp[] = new byte[data.length+SOLT.length];
    	System.arraycopy(data, 0, temp, 0,data.length);
    	System.arraycopy(SOLT, 0, temp, data.length, SOLT.length);
    	
        if (md == null) {
            try {
                writeLock.lock();
                if (md == null) {
                    try {
                        md = MessageDigest.getInstance("MD5");
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                }
            } finally {
                writeLock.unlock();
            }
        }

        byte by[] = null;

        try {
            writeLock.lock();
            md.update(temp);
            by = md.digest();
        } finally {
            writeLock.unlock();
        }
        return new String(by);
    }
    
    public static String getMD5(String password){
    	return getMD5(password.getBytes());
    }
}
