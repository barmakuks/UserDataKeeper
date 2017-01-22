package com.barma.udk.core;

import android.util.Base64;
import com.barma.udk.core.interfaces.ICipher;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

/**
 * @author Created by Vitalii Misiura on 12.12.14.
 * Implementation of ICipher interface.
 *
 * Uses AES algorithm (CTR mode, no padding) to crypt data. Password for AES generated from public key using HMAC based on SHA1.
 * SunJCE: Cipher.AES -> com.sun.crypto.provider.AESCipher
 *
 * Uses SHA1 algorithm for public key hash calculation
 */
public class AesCipher implements ICipher{

    private static final int Iterations = 100;//32768;
    private static final int KeySize = 128;
    private static final String keyAlgorithm = "PBKDF2WithHmacSHA1";
    private static final String hashAlgorithm = "SHA1";
    private static final String secretKeyAlgorithm = "AES";
    private static final byte[] salt = "salt".getBytes();

//      attributes: {SupportedKeyFormats=RAW, SupportedPaddings=NOPADDING|PKCS5PADDING|ISO10126PADDING, SupportedModes=ECB|CBC|PCBC|CTR|CTS|CFB|OFB|CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64|OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64|CFB72|CFB80|CFB88|CFB96|CFB104|CFB112|CFB120|CFB128|OFB72|OFB80|OFB88|OFB96|OFB104|OFB112|OFB120|OFB128}
    private static final String algorithm = "AES/CTR/NOPADDING";

    private final String m_password;

    public AesCipher(String password){
        m_password = password;
    }

    private SecretKeySpec getAesKeyFromPassword(final String password) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(keyAlgorithm);
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, Iterations, KeySize);

        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        return new SecretKeySpec(secretKey.getEncoded(), secretKeyAlgorithm);
    }

    @Override
    public String encrypt(String plain_text) {
        try {
            final Cipher cipher = Cipher.getInstance(algorithm);

            cipher.init(Cipher.ENCRYPT_MODE, getAesKeyFromPassword(m_password));

            AlgorithmParameters params = cipher.getParameters();

            final String iv = Base64.encodeToString(params.getParameterSpec(IvParameterSpec.class).getIV(), Base64.DEFAULT);

            final byte[] cipher_bytes = cipher.doFinal(plain_text.getBytes("UTF-8"));

            return "[" + iv + "]" + Base64.encodeToString(cipher_bytes, Base64.DEFAULT);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidParameterSpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String decrypt(String cipher_text) {
        try {
            final Cipher cipher;
            cipher = Cipher.getInstance(algorithm);

            final String iv = cipher_text.substring(1, cipher_text.indexOf(']'));
            final byte[] ivBytes = Base64.decode(iv, Base64.DEFAULT);

            final String text = cipher_text.substring(cipher_text.indexOf("]") + 1);
            final byte[] textBytes = Base64.decode(text, Base64.DEFAULT);
            cipher.init(Cipher.ENCRYPT_MODE, getAesKeyFromPassword(m_password), new IvParameterSpec(ivBytes));

            final byte[] decoded_bytes = cipher.doFinal(textBytes);

            return new String(decoded_bytes);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getPasswordHash() {
        try {
            byte[] bytesOfMessage = m_password.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
            byte[] digest = md.digest(bytesOfMessage);
            return Base64.encodeToString(digest, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
