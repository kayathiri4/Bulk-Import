package org.wso2.scim.bulkImport.Exceptions;

public class BulkImportException extends Exception{

    public BulkImportException(String message) {
        super(message);
    }

    public BulkImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
