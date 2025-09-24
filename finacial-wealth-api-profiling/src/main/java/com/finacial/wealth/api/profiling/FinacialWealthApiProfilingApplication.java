package com.finacial.wealth.api.profiling;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@EnableFeignClients
@EnableEurekaClient
public class FinacialWealthApiProfilingApplication {

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SpringApplication.run(FinacialWealthApiProfilingApplication.class, args);
        String encryptionKeyPilot = "ed36e39b7de30ad8db7137af61f898ec";
        // String aactTopDebit = encyrpt("4972233445", encryptionKeyPilot);
        String clear = encyrpt("wNZJzgA1oVmFPybMSiKv+g==", encryptionKeyPilot);
        System.out.println("clear " + "  ::::::::::::::::::::: " + clear);
        //System.out.println("password " + "  ::::::::::::::::::::: " + password);

        // String clientidDec = decrypt("sZSmsolo1Lg1sTSF1bFySA==", encryptionKeyPilot);
        //System.out.println("clientidDec " + "  ::::::::::::::::::::: " + clientidDec);
        // String clientidDecWhole = decrypt("ST54anccPPFAD2UfSB6QZ+ypj6aZTdJnHyZ4hITSuRzbONSTf4Z2SY6pUaC3cyMvsruXWRNDk5MOZHE0QJmFBkmmX9hGPyJawZJuyVXD3/A7fYfsMdzTLwBhHdgXIiLDNwdvDyUDQecFrCwgblXkEQ==", encryptionKeyPilot);
        // System.out.println("clientidDecWhole " + "  ::::::::::::::::::::: " + clientidDecWhole);
        // String password = encyrpt("CliPwd$@2024connect", encryptionKeyPilot);
    }

    private static final AtomicLong TS = new AtomicLong();

    public static long getUniqueTimestamp() {
        long micros = System.currentTimeMillis() * 1000;
        for (;;) {
            long value = TS.get();
            if (micros <= value) {
                micros = value + 1;
            }
            if (TS.compareAndSet(value, micros)) {
                return micros;
            }
        }
    }

    public static String encyrpt(String text, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Create key and cipher
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        //Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        // encrypt the text
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(text.getBytes());

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encrypted, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        //Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        // encrypt the text
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        String decrypted = new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
        return decrypted;
    }

}
