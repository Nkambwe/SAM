package com.pbu.sam.exceptions;
/**
 * Class Name: WendiException
 * Extends : System.RuntimeException class
 * Description: Class handles General system exceptions
 * Created By: Nkambwe mark
 */
public class WendiException extends RuntimeException {
    public static final long serialVersionId = 5L;
    public WendiException(String message){
        super(message);
    }
}
