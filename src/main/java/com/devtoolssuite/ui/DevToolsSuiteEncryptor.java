package com.devtoolssuite.ui;

import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleGCMConfig;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleGCMStringEncryptor;

/**
 * Utility class for AES-GCM encryption and decryption using Jasypt's SimpleGCMStringEncryptor.
 * Uses the same logic previously documented.
 */
public class DevToolsSuiteEncryptor {

    private String algorithm;
    private String secretKeyPassword;
    private String secretKeySalt;

    public DevToolsSuiteEncryptor(String algorithm, String secretKeyPassword, String secretKeySalt) {
        this.algorithm = algorithm;
        this.secretKeyPassword = secretKeyPassword;
        this.secretKeySalt = secretKeySalt;
    }

    /**
     * Builds and returns a configured SimpleGCMStringEncryptor.
     */
    private SimpleGCMStringEncryptor buildEncryptor() {
        SimpleGCMConfig config = new SimpleGCMConfig();
        config.setSecretKeyPassword(secretKeyPassword);
        config.setSecretKeyIterations(1000);
        config.setSecretKeySalt(secretKeySalt);
        config.setSecretKeyAlgorithm(algorithm);
        return new SimpleGCMStringEncryptor(config);
    }

    public String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("Plain text cannot be null or empty");
        }
        SimpleGCMStringEncryptor encryptor = buildEncryptor();
        return encryptor.encrypt(plainText);
    }

    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new IllegalArgumentException("Encrypted text cannot be null or empty");
        }
        SimpleGCMStringEncryptor encryptor = buildEncryptor();
        return encryptor.decrypt(encryptedText);
    }

    // Getters and setters
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public String getSecretKeyPassword() { return secretKeyPassword; }
    public void setSecretKeyPassword(String secretKeyPassword) { this.secretKeyPassword = secretKeyPassword; }
    public String getSecretKeySalt() { return secretKeySalt; }
    public void setSecretKeySalt(String secretKeySalt) { this.secretKeySalt = secretKeySalt; }
}
