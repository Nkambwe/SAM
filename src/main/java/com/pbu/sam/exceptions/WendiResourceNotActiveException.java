package com.pbu.sam.exceptions;

/*
 * Class Name: WendiResourceNotFoundException
 * Extends : System.RuntimeException class
 * Description: Class handles inactive resource exceptions. These includes resources that have been deactivated or marked as deleted
 * Created By: Nkambwe mark
 */
public class WendiResourceNotActiveException extends RuntimeException {
    //field is used to ensure the compatibility of serialized objects during the deserialization process.
    private static final long serialVersionId =3L;
    private final String resourceName;
    public String getResourceName() {
        return resourceName;
    }

    private final String fieldName;
    public String getFieldName() {
        return fieldName;
    }

    private final Object fieldValue;
    public Object getFieldValue() {
        return fieldValue;
    }

    public WendiResourceNotActiveException(String resource, String field, Object value){
        super(String.format("Resource Deactivated! %s with %s '%s' is not active or has been deleted.",resource,field, value));
        resourceName = resource;
        fieldName = field;
        fieldValue = value;
    }
}

