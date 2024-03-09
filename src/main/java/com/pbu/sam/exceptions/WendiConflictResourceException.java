package com.pbu.sam.exceptions;
/**
 * Class Name: WendiConflictResourceException
 * Extends : System.RuntimeException class
 * Description: Class handles conflicting resources such as duplicates in the system
 * Created By: Nkambwe mark
 */
public class WendiConflictResourceException extends RuntimeException {

    //field is used to ensure the compatibility of serialized objects during the deserialization process.
    private static final long serialVersionId =2L;
    private String resourceName;
    public String getResourceName() {
        return resourceName;
    }

    private String fieldName;
    public String getFieldName() {
        return fieldName;
    }

    private Object fieldValue;
    public Object getFieldValue() {
        return fieldValue;
    }

    public WendiConflictResourceException(String resource, String field, Object value){
        super(String.format("Resource Conflict! Another %s with %s '%s' found",resource,field, value));
        resourceName = resource;
        fieldName = field;
        fieldValue = value;
    }
}

