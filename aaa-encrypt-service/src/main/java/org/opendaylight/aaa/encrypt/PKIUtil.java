/*
 * Copyright (c) 2017 Brocade Communication Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.sshd.common.util.Base64;
import org.apache.sshd.common.util.SecurityUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

public class PKIUtil{
    private static final String KEY_FACTORY_TYPE_RSA = "RSA";
    private static final String KEY_FACTORY_TYPE_DSA = "DSA";
    private static final String KEY_FACTORY_TYPE_ECDSA = "EC";

    private static final Map<String, String> ECDSA_CURVES = new HashMap<>();

    static {
        ECDSA_CURVES.put("nistp256", "secp256r1");
        ECDSA_CURVES.put("nistp384", "secp384r1");
        ECDSA_CURVES.put("nistp512", "secp512r1");
    }

    private static final String ECDSA_SUPPORTED_CURVE_NAME = "nistp256";
    private static final String ECDSA_SUPPORTED_CURVE_NAME_SPEC = ECDSA_CURVES.get(ECDSA_SUPPORTED_CURVE_NAME);
    private final static int ECDSA_THIRD_STR_LEN = 65;
    private final static int ECDSA_TOTAL_STR_LEN = 104;

    private static final String KEY_TYPE_RSA = "ssh-rsa";
    private static final String KEY_TYPE_DSA = "ssh-dss";
    private static final String KEY_TYPE_ECDSA = "ecdsa-sha2-" + ECDSA_SUPPORTED_CURVE_NAME;

    private byte[] bytes = new byte[0];
    private int pos = 0;

    public PublicKey decodePublicKey(String keyLine) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {

        // look for the Base64 encoded part of the line to decode
        // both ssh-rsa and ssh-dss begin with "AAAA" due to the length bytes
        bytes = Base64.decodeBase64(keyLine.getBytes());
        if (bytes.length == 0)
            throw new IllegalArgumentException("No Base64 part to decode in " + keyLine);
        pos = 0;

        String type = decodeType();
        if (type.equals(KEY_TYPE_RSA))
            return decodeAsRSA();

        if (type.equals(KEY_TYPE_DSA))
            return decodeAsDSA();

        if (type.equals(KEY_TYPE_ECDSA))
            return decodeAsECDSA();

        throw new IllegalArgumentException("Unknown decode key type " + type + " in " + keyLine);
    }

    private PublicKey decodeAsECDSA()
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        KeyFactory ecdsaFactory = SecurityUtils.getKeyFactory(KEY_FACTORY_TYPE_ECDSA);

        ECNamedCurveParameterSpec spec256r1 = ECNamedCurveTable.getParameterSpec(ECDSA_SUPPORTED_CURVE_NAME_SPEC);
        ECNamedCurveSpec params256r1 = new ECNamedCurveSpec(ECDSA_SUPPORTED_CURVE_NAME_SPEC, spec256r1.getCurve(),
                spec256r1.getG(), spec256r1.getN());
        // The total length is 104 bytes, and the X and Y encoding uses the last 65 of these 104 bytes.
        ECPoint point = ECPointUtil.decodePoint(params256r1.getCurve(), Arrays.copyOfRange(bytes,
                ECDSA_TOTAL_STR_LEN - ECDSA_THIRD_STR_LEN, ECDSA_TOTAL_STR_LEN));
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params256r1);

        return ecdsaFactory.generatePublic(pubKeySpec);
    }

    private PublicKey decodeAsDSA() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        KeyFactory dsaFactory = SecurityUtils.getKeyFactory(KEY_FACTORY_TYPE_DSA);
        BigInteger p = decodeBigInt();
        BigInteger q = decodeBigInt();
        BigInteger g = decodeBigInt();
        BigInteger y = decodeBigInt();
        DSAPublicKeySpec spec = new DSAPublicKeySpec(y, p, q, g);

        return dsaFactory.generatePublic(spec);
    }

    private PublicKey decodeAsRSA() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        KeyFactory rsaFactory = SecurityUtils.getKeyFactory(KEY_FACTORY_TYPE_RSA);
        BigInteger exponent = decodeBigInt();
        BigInteger modulus = decodeBigInt();
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);

        return rsaFactory.generatePublic(spec);
    }

    private String decodeType() {
        int len = decodeInt();
        String type = new String(bytes, pos, len);
        pos += len;
        return type;
    }

    private int decodeInt() {
        return ((bytes[pos++] & 0xFF) << 24) | ((bytes[pos++] & 0xFF) << 16)
                | ((bytes[pos++] & 0xFF) << 8) | (bytes[pos++] & 0xFF);
    }

    private BigInteger decodeBigInt() {
        int len = decodeInt();
        byte[] bigIntBytes = new byte[len];
        System.arraycopy(bytes, pos, bigIntBytes, 0, len);
        pos += len;
        return new BigInteger(bigIntBytes);
    }

    public String encodePublicKey(PublicKey publicKey) throws IOException {
        String publicKeyEncoded;
        ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
        if (publicKey.getAlgorithm().equals(KEY_FACTORY_TYPE_RSA)) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            DataOutputStream dataOutputStream = new DataOutputStream(byteOs);
            dataOutputStream.writeInt(KEY_TYPE_RSA.getBytes().length);
            dataOutputStream.write(KEY_TYPE_RSA.getBytes());
            dataOutputStream.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
            dataOutputStream.write(rsaPublicKey.getPublicExponent().toByteArray());
            dataOutputStream.writeInt(rsaPublicKey.getModulus().toByteArray().length);
            dataOutputStream.write(rsaPublicKey.getModulus().toByteArray());
        } else if (publicKey.getAlgorithm().equals(KEY_FACTORY_TYPE_DSA)) {
            DSAPublicKey dsaPublicKey = (DSAPublicKey) publicKey;
            DSAParams dsaParams = dsaPublicKey.getParams();
            DataOutputStream dataOutputStream = new DataOutputStream(byteOs);
            dataOutputStream.writeInt(KEY_TYPE_DSA.getBytes().length);
            dataOutputStream.write(KEY_TYPE_DSA.getBytes());
            dataOutputStream.writeInt(dsaParams.getP().toByteArray().length);
            dataOutputStream.write(dsaParams.getP().toByteArray());
            dataOutputStream.writeInt(dsaParams.getQ().toByteArray().length);
            dataOutputStream.write(dsaParams.getQ().toByteArray());
            dataOutputStream.writeInt(dsaParams.getG().toByteArray().length);
            dataOutputStream.write(dsaParams.getG().toByteArray());
            dataOutputStream.writeInt(dsaPublicKey.getY().toByteArray().length);
            dataOutputStream.write(dsaPublicKey.getY().toByteArray());
        } else if (publicKey.getAlgorithm().equals(KEY_FACTORY_TYPE_ECDSA)) {
            BCECPublicKey ecPublicKey = (BCECPublicKey) publicKey;
            DataOutputStream dataOutputStream = new DataOutputStream(byteOs);
            dataOutputStream.writeInt(KEY_TYPE_ECDSA.getBytes().length);
            dataOutputStream.write(KEY_TYPE_ECDSA.getBytes());
            dataOutputStream.writeInt(ECDSA_SUPPORTED_CURVE_NAME.getBytes().length);
            dataOutputStream.write(ECDSA_SUPPORTED_CURVE_NAME.getBytes());
            byte[] x = ecPublicKey.getQ().getAffineXCoord().getEncoded();
            byte[] y = ecPublicKey.getQ().getAffineYCoord().getEncoded();
            dataOutputStream.writeInt(x.length + y.length + 1);
            dataOutputStream.writeByte(0x04);
            dataOutputStream.write(x);
            dataOutputStream.write(y);
        } else {
            throw new IllegalArgumentException("Unknown public key encoding: " + publicKey.getAlgorithm());
        }
        publicKeyEncoded = new String(Base64.encodeBase64(byteOs.toByteArray()));
        return publicKeyEncoded;

    }

    public KeyPair decodePrivateKey(String keyPath, String passphrase) throws IOException {
        FileReader fileReader = new FileReader(keyPath);
        PEMParser keyReader = new PEMParser(fileReader);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        PEMDecryptorProvider decryptionProv = new JcePEMDecryptorProviderBuilder().build(passphrase.toCharArray());

        Object privateKey = keyReader.readObject();
        KeyPair keyPair;
        if (privateKey instanceof PEMEncryptedKeyPair) {
            PEMKeyPair decryptedKeyPair = ((PEMEncryptedKeyPair) privateKey).decryptKeyPair(decryptionProv);
            keyPair = converter.getKeyPair(decryptedKeyPair);
        } else {
            keyPair = converter.getKeyPair((PEMKeyPair) privateKey);
        }

        keyReader.close();
        return keyPair;
    }

}
