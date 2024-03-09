package com.pbu.sam.common;

import com.pbu.sam.utils.Literals;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Class Name :Secure
 * Created By : Nkambwe Mark
 * Description:Class handles both encryption and decryption related to the application
 **/
public class Secure {

    /**
     * Hash user password
     * {@code @password} - User password to hash
     * {@code @salt} - Password salt */
    public static String hashPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        // Number of iterations - You can adjust this based on your security requirements
        int iterations = 10000;

        // Key length in bits
        int keyLength = 512;

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(Literals.HASH_KEY);
        byte[] hash = skf.generateSecret(spec).getEncoded();

        // Combine the salt and hash
        byte[] combined = new byte[salt.length + hash.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(hash, 0, combined, salt.length, hash.length);

        // Convert to Base64 and return
        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     *Verify user password
     * {@code @password} - password to verify
     * {@code @hashedPassword} - user hashed password in database
     * {@code @Salt} - random salt value
     * @return true if successfully verified, else false
     */
    public static boolean verifyPassword(String password, String hashedPassword, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        // Decode the hashed password
        byte[] decodedHash = Base64.getDecoder().decode(hashedPassword);

        // Extract the salt from the stored hash
        byte[] extractedSalt = Arrays.copyOfRange(decodedHash, 0, salt.length);

        // Hash the provided password with the extracted salt
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), extractedSalt, 10000, 512);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(Literals.HASH_KEY);
        byte[] computedHash = skf.generateSecret(spec).getEncoded();

        // Extract the actual hash stored in the hashed password
        byte[] storedHash = Arrays.copyOfRange(decodedHash, salt.length, decodedHash.length);

        // Compare the computed hash with the extracted stored hash
        return Arrays.equals(storedHash, computedHash);
    }

    /**
     * Method Encrypts string value
     * {@code @strToEncrypt} - String value to encrypt
     * {@code @secret} - Secret key to encrypt value
     * {@code @logger} - Logger class object
     * {@code @field} - Object field being encrypted
     * @return encrypted string
     */
    public static String encrypt(String strToEncrypt, String secret, AppLoggerService logger, String field) {
        try {
            SecretKeySpec secretKey = prepareKey(secret, logger);
            Cipher cipher = Cipher.getInstance(Literals.ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            logger.info(String.format("Error while encrypting '%s': '%s'", field, e.getMessage()));
        }
        return null;
    }

    /**
     * Method Decrypts string value
     * {@code @strToDecrypt} - String value to decrypt
     * {@code @secret} - Secret key to decrypt value
     * {@code @logger} - Logger class object
     * {@code @field} - Object field being encrypted
     * @return decrypted string
     */
    public static String decrypt(String strToDecrypt, String secret, AppLoggerService logger, String field) {
        try {
            logger.info(String.format("Decrypting '%s'...", field));
            SecretKeySpec secretKey = prepareKey(secret, logger);
            Cipher cipher = Cipher.getInstance(Literals.ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            logger.info(String.format("Error while decrypting '%s': '%s'", field, e.getMessage()));
        }
        return null;
    }

    private static SecretKeySpec prepareKey(String myKey, AppLoggerService logger) {
        SecretKeySpec secretKey = null;
        logger.info("Preparing secret key...");
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, Literals.ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            logger.error(String.format("Preparing secret:: NoSuchAlgorithmException Occurred. Message '%s'", e.getMessage()));
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(e);
            logger.stackTrace(stackTrace);
        }

        return secretKey;
    }
}

