package com.uniq.tms.tms_microservice.shared.exception;

public class CommonExceptionHandler {

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }

    public static class DuplicateUserException extends RuntimeException {
        public DuplicateUserException(String message) {
            super(message);
        }
    }

    public static class NoUserLocationAssignedException extends RuntimeException {
        public NoUserLocationAssignedException(String message){
            super("No locations assigned to logged user");
        }
    }

    public static class InternalServerException extends RuntimeException {
        public InternalServerException(String message){
            super(message);
        }
    }

    public static class SchemaNotFoundException extends RuntimeException{
        public SchemaNotFoundException(String message){super(message);}
    }

    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }
    }

    public static class DefaultLocationNotFoundException extends RuntimeException {
        public DefaultLocationNotFoundException(String message) {
            super(message);
        }
    }

}
