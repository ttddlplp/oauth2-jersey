package com.example.token.cryptography;

import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class AesWithHmacCryptoCodecTest {

    @Test
    public void produceDemoOutput() throws Exception {
        String toEncrypt = "This string amounts to about two lines of text. It "
            + "gives us an idea of the encryption overhead.";

        AesWithHmacCryptoCodec instance = new AesWithHmacCryptoCodec(hex("000102030405060708090a0b0c0d0e0f"), 4096);
        byte[] ciphertextA = instance.encrypt(toEncrypt.getBytes("UTF-8"));

        String base64 = DatatypeConverter.printBase64Binary(ciphertextA);
        System.out.println(base64 + " (" + base64.length() + " bytes)");

        System.out.println("With input length " + toEncrypt.length() + " overhead is " + (base64.length() - toEncrypt.length()));
    }

    @Test
    public void testEncryptDecrypt() throws Exception {
        byte[] plaintextIn = "A man, a plan, a canal, panama".getBytes("UTF-8");

        AesWithHmacCryptoCodec instance = new AesWithHmacCryptoCodec(hex("000102030405060708090a0b0c0d0e0f"), 4096);
        byte[] ciphertext = instance.encrypt(plaintextIn);
        byte[] plaintextOut = instance.decrypt(ciphertext);

        assertArrayEquals(plaintextIn, plaintextOut);
    }

    @Test
    public void testEncryptSimilar() throws Exception {
        System.out.println("Similar plaintext should produce very different ciphertext.");

        AesWithHmacCryptoCodec instance = new AesWithHmacCryptoCodec(hex("000102030405060708090a0b0c0d0e0f"), 4094);
        byte[] ciphertextA = instance.encrypt("Message A".getBytes("UTF-8"));
        byte[] ciphertextB = instance.encrypt("Message B".getBytes("UTF-8"));

        print(ciphertextA);
        print(ciphertextB);

        int commonBytes = 0;
        for (int i=0 ; i<ciphertextA.length ; i++) {
            if (ciphertextA[i]==ciphertextB[i]) {
                commonBytes++;
            }
        }

        System.out.println(commonBytes + " bytes common out of " + ciphertextA.length);
        assertTrue(commonBytes < (ciphertextA.length/10));
    }

    @Test(expected = GeneralSecurityException.class)
    public void testDecryptFailureEndMissing() throws Exception {
        byte[] plaintextIn = "A man, a plan, a canal, panama".getBytes("UTF-8");
        AesWithHmacCryptoCodec instance = new AesWithHmacCryptoCodec(hex("000102030405060708090a0b0c0d0e0f"), 4096);
        byte[] ciphertext = instance.encrypt(plaintextIn);

        byte[] truncatedCiphertext = Arrays.copyOf(ciphertext, ciphertext.length - 1);
        instance.decrypt(truncatedCiphertext);
    }

    @Test(expected = GeneralSecurityException.class)
    public void testDecryptFailureCharacterChanged() throws Exception {
        byte[] plaintextIn = "A man, a plan, a canal, panama".getBytes("UTF-8");
        AesWithHmacCryptoCodec instance = new AesWithHmacCryptoCodec(hex("000102030405060708090a0b0c0d0e0f"), 4096);
        byte[] ciphertext = instance.encrypt(plaintextIn);

        ciphertext[ciphertext.length/2]++;

        instance.decrypt(ciphertext);
    }

    /**
     * The encryption algorithm pads the plaintext out to the nearest complete
     * block. Often, if you mess with the message in transit, the padding
     * decrypts wrong and that's easy to detect. But there's also a chance
     * (probability depends on the length of the padding) the decrypted
     * padding will look right - in this case we expect a signature failure.
     */
    @Test(expected = GeneralSecurityException.class)
    public void testDecryptFailureButPaddingRight() throws Exception {
        byte[] plaintextIn = hex("00112233445566778899aabbccddee"); // Chosen so we get 1 byte of padding
        AesWithHmacCryptoCodec instance = new AesWithHmacCryptoCodec(hex("000102030405060708090a0b0c0d0e0f"), 4096);
        byte[] ciphertext = instance.encrypt(plaintextIn);

        byte[] secretKeyBytes = hex("00000000000000000000000000000000");

        // Find a secret key that hits on the right padding byte.
        for (int i=0 ; i<256 ; i++) {
            secretKeyBytes[0]=(byte)i;
            try {
                AesWithHmacCryptoCodec instance2 = new AesWithHmacCryptoCodec(secretKeyBytes, 4096);
                instance2.decrypt(ciphertext);
            } catch (GeneralSecurityException e) {
                if (!e.getMessage().contains("padding")) {
                    System.out.println("By chance, got the padding right when i="+i);
                    break;
                }
            }
        }

        AesWithHmacCryptoCodec instance2 = new AesWithHmacCryptoCodec(secretKeyBytes, 4096);
        instance2.decrypt(ciphertext);
    }

    @Test
    public void testHashHmacSHA256() throws Exception {
        System.out.println("hashHmacSHA256");
        // Test values from http://en.wikipedia.org/w/index.php?title=Hash-based_message_authentication_code&oldid=602397067#Examples_of_HMAC_.28MD5.2C_SHA1.2C_SHA256.29
        byte[] plaintextBytes = "The quick brown fox jumps over the lazy dog".getBytes("UTF-8");
        AesWithHmacCryptoCodec instance = new AesWithHmacCryptoCodec("key".getBytes("UTF-8"), 4096);
        byte[] expResult = hex("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
        byte[] result = instance.hashHmacSHA256(plaintextBytes);
        assertArrayEquals(expResult, result);
    }

    public static byte[] hex(String s) {
        return DatatypeConverter.parseHexBinary(s);
    }

    public static void print(byte[] bytes) {
        System.out.println(DatatypeConverter.printHexBinary(bytes));
    }

}

