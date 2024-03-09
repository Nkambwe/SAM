package com.pbu.sam.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
/**
 * Class Name: WendiExceptionHandler
 * Description: Exception handling class
 * Returns: Class implements all system exceptions
 * Created By: Nkambwe mark
 */
@ControllerAdvice
public class WendiExceptionHandler {

    /**
     * Handle resource not found exceptions
     * remarks.Class throws error message when system resource is not found
     * @param e - WendiResourceNotFoundException object
     * @param request - HttpServletRequest object
     * @return Status code {@code HttpStatus.NOT_FOUND} - Resource not found
     **/
    @ExceptionHandler(WendiResourceNotFoundException.class)
    public ResponseEntity<WendiError> resourceNotFoundExceptionHandler(WendiResourceNotFoundException e, HttpServletRequest request){
        WendiError error = new WendiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle Duplicate resource exceptions
     * remarks.Class throws error message when system resource exists and a possible duplicate if posted
     * @param e - WendiConflictResourceException object
     * @param request - HttpServletRequest object
     * @return Status code {@code HttpStatus.CONFLICT} - Possible duplication causing conflict
     **/
    @ExceptionHandler(WendiConflictResourceException.class)
    public ResponseEntity<WendiError> duplicatesResourceExceptionHandler(WendiConflictResourceException e, HttpServletRequest request){
        WendiError error = new WendiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handle authorization credentials exceptions
     * remarks.Class throws error message when user's credentials are not valid or not allowed to access resource
     * @param e - WendiResourceNotActiveException object
     * @param request - HttpServletRequest object
     * @return Status code {@code HttpStatus.UNAUTHORIZED} - User not authorized to access resource
     **/
    @ExceptionHandler(WendiBadCredentialsException.class)
    public ResponseEntity<WendiError> badCredentialsExceptionHandler(WendiBadCredentialsException e, HttpServletRequest request){
        WendiError error = new WendiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle inactive or deleted resource exceptions
     * remarks.Class throws error message when system resource is marked as deleted or inactive
     * @param e - WendiResourceNotActiveException object
     * @param request - HttpServletRequest object
     * @return Status code {@code HttpStatus.GONE} - Resource no longer available
     **/
    @ExceptionHandler(WendiResourceNotActiveException.class)
    public ResponseEntity<WendiError> resourceInactiveExceptionHandler(WendiResourceNotActiveException e, HttpServletRequest request){
        WendiError error = new WendiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.GONE.value(),//GONE - resource no longer available
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.GONE);
    }

    /**
     * Handle resource validation exceptions
     * remarks.Class throws error message when system resource is invalid
     * @param e - WendiRequestValidationException object
     * @param request - HttpServletRequest object
     * @return Status code {@code HttpStatus.BAD_REQUEST} - Client sent a bad request
     **/
    @ExceptionHandler(WendiRequestValidationException.class)
    public ResponseEntity<WendiError> validationExceptionHandler(WendiRequestValidationException e, HttpServletRequest request){
        WendiError error = new WendiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle system server errors exceptions
     * remarks.Class throws error message when system resource is invalid
     * @param e - WendiRequestValidationException object
     * @param request - Exception object
     * @return Status code {@code HttpStatus.INTERNAL_SERVER_ERROR} - Server Error
     **/
    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<WendiError> systemErrorExceptionHandler(Exception e, HttpServletRequest request){
        WendiError error = new WendiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Handle system thread interrupted exceptions
     * remarks.Class throws error message when system resource is invalid
     * @param e - WendiRequestValidationException object
     * @param request - Exception object
     * @return Status code {@code HttpStatus.EXPECTATION_FAILED} - Client Error
     **/
    @ExceptionHandler(WendiThreadCanceledException.class)
    public ResponseEntity<WendiError> threadCanceledHandler(WendiThreadCanceledException e, HttpServletRequest request){
        WendiError error = new WendiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.EXPECTATION_FAILED.value(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.EXPECTATION_FAILED);
    }

    /**
     * Handle General System exceptions
     * remarks.Class throws error WendiException when system has a general exception
     * @param e - WendiResourceNotActiveException object
     * @param request - HttpServletRequest object
     * @return Status code {@code HttpStatus.INTERNAL_SERVER_ERROR} - Server error
     **/
    @ExceptionHandler(WendiException.class)
    public ResponseEntity<WendiError> exceptionHandler(WendiException e, HttpServletRequest request){
        WendiError error = new WendiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle Bad request exceptions
     * remarks.Class throws error message when system has a general exception
     * @param e - WendiClientException object
     * @param request - HttpServletRequest object
     * @return Status code {@code HttpStatus.BAD_REQUEST} - Server error
     **/
    @ExceptionHandler(WendiClientException.class)
    public ResponseEntity<WendiError> clientErrorHandler(WendiClientException e, HttpServletRequest request){
        WendiError error = new WendiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
