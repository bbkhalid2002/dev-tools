package com.jasypt.ui;

import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleGCMConfig;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleGCMStringEncryptor;

/**
 * Utility class for Jasypt AES-GCM encryption and decryption.
 * Uses the exact logic from jasypt-logic.txt
 */
public class JasyptEncryptor {

    private String algorithm;
    private String secretKeyPassword;
    private String secretKeySalt;

    public JasyptEncryptor(String algorithm, String secretKeyPassword, String secretKeySalt) {
        this.algorithm = algorithm;
        this.secretKeyPassword = secretKeyPassword;
        this.secretKeySalt = secretKeySalt;
    }

    /**
     * Builds and returns a configured SimpleGCMStringEncryptor.
     * This matches the exact logic from jasypt-logic.txt
     */
    private SimpleGCMStringEncryptor buildEncryptor() {
        SimpleGCMConfig config = new SimpleGCMConfig();
        config.setSecretKeyPassword(secretKeyPassword);
        config.setSecretKeyIterations(1000);
        config.setSecretKeySalt(secretKeySalt);
        config.setSecretKeyAlgorithm(algorithm);
        return new SimpleGCMStringEncryptor(config);
    }

    /**
     * Encrypts the given plain text value.
     *
     * @param plainText The text to encrypt
     * @return The encrypted value
     * @throws Exception if encryption fails
     */
    public String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("Plain text cannot be null or empty");
        }

        SimpleGCMStringEncryptor encryptor = buildEncryptor();
        return encryptor.encrypt(plainText);
    }

    /**
     * Decrypts the given encrypted value.
     *
     * @param encryptedText The text to decrypt
     * @return The decrypted plain text value
     * @throws Exception if decryption fails
     */
    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new IllegalArgumentException("Encrypted text cannot be null or empty");
        }

        SimpleGCMStringEncryptor encryptor = buildEncryptor();
        return encryptor.decrypt(encryptedText);
    }

    // Getters and setters
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getSecretKeyPassword() {
        return secretKeyPassword;
    }

    public void setSecretKeyPassword(String secretKeyPassword) {
        this.secretKeyPassword = secretKeyPassword;
    }

    public String getSecretKeySalt() {
        return secretKeySalt;
    }

    public void setSecretKeySalt(String secretKeySalt) {
        this.secretKeySalt = secretKeySalt;
    }
}
