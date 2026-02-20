/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

/**
 *
 * @author olufemioshin
 */
import java.io.IOException;
import org.bouncycastle.asn1.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ReceiptSignRequest;
import org.springframework.stereotype.Service;
@Service
public final class ReceiptSignatureUtil {

    private ReceiptSignatureUtil() {}

    // P-256 order (n)
    private static final BigInteger P256_N = new BigInteger(
            "FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16);
    private static final BigInteger P256_HALF_N = P256_N.shiftRight(1);

    // EXACT canonical format agreed with mobile
    public static String canonicalV1(ReceiptSignRequest p) {
        return "v=1"
                + "|txId=" + p.getTxId()
                + "|amountMinor=" + p.getAmountMinor()
                + "|currency=" + p.getCurrency()
                + "|senderId=" + p.getSenderId()
                + "|receiverId=" + p.getReceiverId()
                + "|timestamp=" + p.getTimestampUtcIso()
                + "|status=" + p.getStatus();
    }

    // Sign canonical string -> base64(raw 64 bytes R||S), low-S enforced
    public static String signRawRsBase64(PrivateKey privateKey, String canonicalPayload) {
        try {
            byte[] msg = canonicalPayload.getBytes(StandardCharsets.UTF_8);

            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initSign(privateKey);
            sig.update(msg);

            byte[] der = sig.sign(); // ASN.1 DER signature
            BigInteger[] rs = derToRs(der);

            BigInteger r = rs[0];
            BigInteger s = rs[1];

            // low-S canonicalization
            if (s.compareTo(P256_HALF_N) > 0) {
                s = P256_N.subtract(s);
            }

            byte[] raw = rsToRaw64(r, s);
            return java.util.Base64.getEncoder().encodeToString(raw);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Signing failed", e);
        }
    }

    // Optional: verifier (useful for tests)
    public static boolean verifyRawRsBase64(String publicKeySpkiBase64, String canonicalPayload, String rawRsBase64) throws IOException {
        try {
            byte[] spki = java.util.Base64.getDecoder().decode(publicKeySpkiBase64);
            KeyFactory kf = KeyFactory.getInstance("EC");
            PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(spki));

            byte[] raw = java.util.Base64.getDecoder().decode(rawRsBase64);
            if (raw.length != 64) return false;

            BigInteger r = new BigInteger(1, Arrays.copyOfRange(raw, 0, 32));
            BigInteger s = new BigInteger(1, Arrays.copyOfRange(raw, 32, 64));

            // strict: reject high-S
            if (s.compareTo(P256_HALF_N) > 0) return false;

            byte[] der = rsToDer(r, s);

            Signature verifier = Signature.getInstance("SHA256withECDSA");
            verifier.initVerify(pub);
            verifier.update(canonicalPayload.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(der);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    private static BigInteger[] derToRs(byte[] derSig) {
        ASN1Sequence seq = ASN1Sequence.getInstance(derSig);
        BigInteger r = ((ASN1Integer) seq.getObjectAt(0)).getValue();
        BigInteger s = ((ASN1Integer) seq.getObjectAt(1)).getValue();
        return new BigInteger[]{r, s};
    }

    private static byte[] rsToDer(BigInteger r, BigInteger s) throws GeneralSecurityException, IOException {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(r));
        v.add(new ASN1Integer(s));
        return new DERSequence(v).getEncoded();
    }

    private static byte[] rsToRaw64(BigInteger r, BigInteger s) {
        byte[] rb = toFixed32(r);
        byte[] sb = toFixed32(s);
        byte[] out = new byte[64];
        System.arraycopy(rb, 0, out, 0, 32);
        System.arraycopy(sb, 0, out, 32, 32);
        return out;
    }

    private static byte[] toFixed32(BigInteger x) {
        byte[] b = x.toByteArray();
        byte[] out = new byte[32];
        int start = Math.max(0, b.length - 32);
        int len = Math.min(32, b.length);
        System.arraycopy(b, start, out, 32 - len, len);
        return out;
    }
}
