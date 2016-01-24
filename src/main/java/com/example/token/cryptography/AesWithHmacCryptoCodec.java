package com.example.token.cryptography;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

public class AesWithHmacCryptoCodec {

    private static final int IV_LENGTH_BYTES = 16;
    private static final int SIGNATURE_LENGTH_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final int maxDecryptLengthBytes;
    private final byte[] secretKeyBytes;

    public AesWithHmacCryptoCodec(byte[] secretKeyBytes, int maxDecryptLengthBytes) {
        this.secretKeyBytes = secretKeyBytes;
        this.maxDecryptLengthBytes = maxDecryptLengthBytes;
    }

    public byte[] encrypt(byte[] plaintextBytes) throws GeneralSecurityException {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        byte[] cipherText = encryptAesCbc(iv, plaintextBytes);
        byte[] cipherTextWithIv = joinByteArrays(iv, cipherText);
        byte[] signature = hashHmacSHA256(cipherTextWithIv);

        return joinByteArrays(cipherTextWithIv, signature);
    }

    public byte[] decrypt(byte[] encryptedBytes) throws GeneralSecurityException {
        if (encryptedBytes.length <= IV_LENGTH_BYTES + SIGNATURE_LENGTH_BYTES) {
            throw new GeneralSecurityException("Not enough bytes to decrypt");
        } else if (encryptedBytes.length > maxDecryptLengthBytes) {
            throw new GeneralSecurityException("Too many bytes to decrypt");
        }

        byte[] iv = Arrays.copyOf(encryptedBytes, IV_LENGTH_BYTES);
        byte[] ciphertext = Arrays.copyOfRange(encryptedBytes, IV_LENGTH_BYTES, encryptedBytes.length- SIGNATURE_LENGTH_BYTES);
        byte[] signature = Arrays.copyOfRange(encryptedBytes, encryptedBytes.length - SIGNATURE_LENGTH_BYTES, encryptedBytes.length);

        byte[] requiredSignature = hashHmacSHA256(joinByteArrays(iv, ciphertext));
        if (!Arrays.equals(signature, requiredSignature)) {
            throw new GeneralSecurityException("Incorrect HMAC-SHA256 signature, data may have been tampered with");
        }

        try {
            return decryptAesCbc(iv, ciphertext);
        } catch (IllegalBlockSizeException e) {
            throw new GeneralSecurityException("During decryption, the ciphertext block size was wrong");
        } catch (BadPaddingException e) {
            throw new GeneralSecurityException("After decrypting, the PKCS5 padding was wrong");
        }
    }

    private byte[] joinByteArrays(byte[] a, byte[] b) {
        byte[] joined = new byte[a.length + b.length];
        System.arraycopy(a, 0, joined, 0, a.length);
        System.arraycopy(b, 0, joined, a.length, b.length);
        return joined;
    }

    private byte[] encryptAesCbc(byte[] ivBytes, byte[] plaintextBytes) throws GeneralSecurityException {
        SecretKeySpec encryptionKey = new SecretKeySpec(secretKeyBytes, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, iv);
        return cipher.doFinal(plaintextBytes);
    }

    private byte[] decryptAesCbc(byte[] ivBytes, byte[] ciphertextBytes) throws GeneralSecurityException {
        SecretKeySpec encryptionKey = new SecretKeySpec(secretKeyBytes, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, iv);
        return cipher.doFinal(ciphertextBytes);
    }

    protected byte[] hashHmacSHA256(byte[] plaintextBytes) throws GeneralSecurityException {
        SecretKeySpec signingKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        return mac.doFinal(plaintextBytes);
    }
}

