package com.pbu.sam.exceptions;
/**
 * Class Name: WendiBadCredentialsException
 * Extends : System.RuntimeException class
 * Description: Class handles Authentication credential errors
 * Created By: Nkambwe mark
 */
public class WendiRequestValidationException extends Exception {
    public static final long serialVersionId = 1L;
    public WendiRequestValidationException(String message){
        super(message);
    }
}
