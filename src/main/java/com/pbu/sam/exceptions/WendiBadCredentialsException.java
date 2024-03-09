package com.pbu.sam.exceptions;
/**
 * Class Name: WendiBadCredentialsException
 * Extends : System.RuntimeException class
 * Description: Class handles Authentication credential errors
 * Created By: Nkambwe mark
 */
public class WendiBadCredentialsException extends RuntimeException {
    public static final long serialVersionId = 1L;
    public WendiBadCredentialsException(String message){
        super(message);
    }
}
