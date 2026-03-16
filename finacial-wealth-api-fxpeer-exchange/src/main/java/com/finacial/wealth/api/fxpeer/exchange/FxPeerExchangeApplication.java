package com.finacial.wealth.api.fxpeer.exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class FxPeerExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FxPeerExchangeApplication.class, args);

        try {
           // runConsentSignatureDebug();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runConsentSignatureDebug() throws Exception {
        String canonical = "v1|CONSENT|POST|/api/transactions/localtransfer/transfer/non-pin"
                + "|refId=177280390754883853"
                + "|payloadHashB64=TkJ6LVUM0wFroz60/r8qJ4jmW+gs4DMzZ8YIGgjnwmI="
                + "|ts=1772887490000"
                + "|nonce=4c15af6a6108530e7ae03ad1"
                + "|userId=mp@elara-solutions.com"
                + "|deviceId=D1D22D1D-3E64-4212-A572-2606E2460F24"
                + "|kid=d_20260306_fd9560";

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        KeyPair kp = kpg.generateKeyPair();

        byte[] spki = kp.getPublic().getEncoded();
        String spkiB64 = Base64.getEncoder().encodeToString(spki);

        System.out.println("publicSpkiB64=" + spkiB64);
        System.out.println("publicSpki.sha256=" + sha256B64(spki));
        System.out.println("canonical=" + canonical);
        System.out.println("canonical.sha256=" + sha256B64(canonical.getBytes(StandardCharsets.UTF_8)));

        byte[] derSig = signDer(kp.getPrivate(), canonical.getBytes(StandardCharsets.UTF_8));
        String derSigB64 = Base64.getEncoder().encodeToString(derSig);

        System.out.println("derSig.len=" + derSig.length);
        System.out.println("derSig.b64=" + derSigB64);

        boolean okDer = verifyDer(kp.getPublic(), canonical.getBytes(StandardCharsets.UTF_8), derSig);
        System.out.println("verify DER directly = " + okDer);

        byte[] raw64 = derToP1363(derSig);
        System.out.println("raw64.len=" + raw64.length);
        System.out.println("raw64.b64=" + Base64.getEncoder().encodeToString(raw64));

        boolean okRawAsDer = verifyDer(kp.getPublic(), canonical.getBytes(StandardCharsets.UTF_8), raw64);
        System.out.println("verify raw64 as DER = " + okRawAsDer);

        byte[] backToDer = p1363ToDer(raw64);
        boolean okRawConverted = verifyDer(kp.getPublic(), canonical.getBytes(StandardCharsets.UTF_8), backToDer);
        System.out.println("verify raw64 after conversion = " + okRawConverted);

        PublicKey reparsed = parsePublicKey(spkiB64);
        boolean okReparsed = verifyDer(reparsed, canonical.getBytes(StandardCharsets.UTF_8), derSig);
        System.out.println("verify with reparsed public key = " + okReparsed);
    }

    private static byte[] signDer(PrivateKey privateKey, byte[] data) throws Exception {
        Signature s = Signature.getInstance("SHA256withECDSA");
        s.initSign(privateKey);
        s.update(data);
        return s.sign();
    }

    private static boolean verifyDer(PublicKey publicKey, byte[] data, byte[] derSig) {
        try {
            Signature v = Signature.getInstance("SHA256withECDSA");
            v.initVerify(publicKey);
            v.update(data);
            return v.verify(derSig);
        } catch (Exception e) {
            System.out.println("verify error: " + e.getMessage());
            return false;
        }
    }

    private static PublicKey parsePublicKey(String publicSpkiB64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicSpkiB64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("EC").generatePublic(spec);
    }

    private static String sha256B64(byte[] input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return Base64.getEncoder().encodeToString(md.digest(input));
    }

    private static byte[] p1363ToDer(byte[] sig) {
        if (sig == null || sig.length != 64) {
            throw new IllegalArgumentException("Expected 64-byte P1363 signature");
        }

        byte[] r = Arrays.copyOfRange(sig, 0, 32);
        byte[] s = Arrays.copyOfRange(sig, 32, 64);
        return derEncode(r, s);
    }

    private static byte[] derEncode(byte[] r, byte[] s) {
        byte[] derR = derInteger(r);
        byte[] derS = derInteger(s);

        int seqLen = 2 + derR.length + 2 + derS.length;
        byte[] out = new byte[2 + seqLen];

        int i = 0;
        out[i++] = 0x30;
        out[i++] = (byte) seqLen;

        out[i++] = 0x02;
        out[i++] = (byte) derR.length;
        System.arraycopy(derR, 0, out, i, derR.length);
        i += derR.length;

        out[i++] = 0x02;
        out[i++] = (byte) derS.length;
        System.arraycopy(derS, 0, out, i, derS.length);

        return out;
    }

    private static byte[] derInteger(byte[] value) {
        int firstNonZero = 0;
        while (firstNonZero < value.length - 1 && value[firstNonZero] == 0) {
            firstNonZero++;
        }

        byte[] stripped = Arrays.copyOfRange(value, firstNonZero, value.length);

        if (stripped.length == 0) {
            return new byte[]{0x00};
        }

        if ((stripped[0] & 0x80) != 0) {
            byte[] prefixed = new byte[stripped.length + 1];
            prefixed[0] = 0x00;
            System.arraycopy(stripped, 0, prefixed, 1, stripped.length);
            return prefixed;
        }

        return stripped;
    }

    private static byte[] derToP1363(byte[] der) {
        if (der == null || der.length < 8 || der[0] != 0x30) {
            throw new IllegalArgumentException("Invalid DER signature");
        }

        int idx = 2;

        if (der[idx++] != 0x02) {
            throw new IllegalArgumentException("Invalid DER signature: missing R integer");
        }
        int rLen = der[idx++] & 0xff;
        byte[] r = Arrays.copyOfRange(der, idx, idx + rLen);
        idx += rLen;

        if (der[idx++] != 0x02) {
            throw new IllegalArgumentException("Invalid DER signature: missing S integer");
        }
        int sLen = der[idx++] & 0xff;
        byte[] s = Arrays.copyOfRange(der, idx, idx + sLen);

        byte[] raw = new byte[64];
        copyTo32(r, raw, 0);
        copyTo32(s, raw, 32);
        return raw;
    }

    private static void copyTo32(byte[] src, byte[] dest, int offset) {
        int start = 0;
        while (start < src.length - 1 && src[start] == 0) {
            start++;
        }

        int len = src.length - start;
        if (len > 32) {
            throw new IllegalArgumentException("Integer too large");
        }

        System.arraycopy(src, start, dest, offset + (32 - len), len);
    }

    // AES-GCM version (recommended)
    public static String encrypt(String plainText, String hexKey) throws Exception {
        byte[] keyBytes = hexToBytes(hexKey);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        byte[] iv = new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));

        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
        buffer.put(iv);
        buffer.put(cipherText);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    public static String decrypt(String encryptedBase64, String hexKey) throws Exception {
        byte[] allBytes = Base64.getDecoder().decode(encryptedBase64);
        byte[] keyBytes = hexToBytes(hexKey);

        ByteBuffer buffer = ByteBuffer.wrap(allBytes);
        byte[] iv = new byte[12];
        buffer.get(iv);

        byte[] cipherBytes = new byte[buffer.remaining()];
        buffer.get(cipherBytes);

        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

        byte[] plain = cipher.doFinal(cipherBytes);
        return new String(plain, StandardCharsets.UTF_8);
    }

    private static byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex key");
        }

        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            out[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return out;
    }
}
