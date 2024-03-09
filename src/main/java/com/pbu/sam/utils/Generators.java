package com.pbu.sam.utils;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.pbu.sam.common.AppLoggerService;
import com.pbu.sam.common.Secure;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

public class Generators {

    /*Generate the string equivalent of the current date*/
    public static String currentDate() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentDateTime.format(formatter);
    }

    /*Convert date to string*/
    public static String dateToString(LocalDateTime dateTime) {
        // Define the desired date and time format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Format the date and time as a string
        return dateTime.format(formatter);
    }

    public static String getHashedPassword(String password, AppLoggerService logger) {
        logger.info("Hashing password...");
        String hashedPassword = null;
        try {
            logger.info(String.format("Original Password is '%s'", password));
            hashedPassword = Secure.hashPassword(password, Literals.SALT.getBytes());
            logger.info(String.format("Hashed Password is '%s'", hashedPassword));
        } catch (NoSuchAlgorithmException e) {
            logger.error(String.format("NoSuchAlgorithmException Occurred. Message '%s'", e.getMessage()));
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(e);
            logger.stackTrace(stackTrace);
        } catch (InvalidKeySpecException e) {
            RuntimeException ex = new RuntimeException(e);
            logger.error(String.format("InvalidKeySpecException Occurred. Message '%s'", ex.getMessage()));
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(e);
            logger.stackTrace(stackTrace);
        }

        return hashedPassword;
    }

    public static boolean isPasswordMatch(String password, String hashedPassword, AppLoggerService logger){
        logger.info("Verifying password...");
        boolean isMatched = false;
        try {
            logger.info(String.format("Original Password :: '%s'", password));
            logger.info(String.format("Hashed Password is '%s'", hashedPassword));
            isMatched = Secure.verifyPassword(password, hashedPassword, Literals.SALT.getBytes());
        } catch (NoSuchAlgorithmException e) {
            logger.error(String.format("NoSuchAlgorithmException Occurred. Message '%s'", e.getMessage()));
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(e);
            logger.stackTrace(stackTrace);
        } catch (InvalidKeySpecException e) {
            RuntimeException ex = new RuntimeException(e);
            logger.error(String.format("InvalidKeySpecException Occurred. Message '%s'", ex.getMessage()));
            logger.info("StackTrace Details >>>>>");
            String stackTrace = ExceptionUtils.getStackTrace(e);
            logger.stackTrace(stackTrace);
        }
        return isMatched;
    }
    /*Generate error message from BindingResult object*/
    public static String buildErrorMessage(BindingResult bindingResult){
        List<FieldError> fields = bindingResult.getFieldErrors();
        StringBuilder message = new StringBuilder("Validation failed for fields: ");

        for (FieldError field : fields) {
            message.append(field.getField())
                    .append(" - ")
                    .append(field.getDefaultMessage())
                    .append(", ");
        }

        // Remove the trailing comma and space
        message.setLength(message.length() - 2);

        return message.toString();
    }
}
