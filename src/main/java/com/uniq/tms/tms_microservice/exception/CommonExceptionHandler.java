package com.uniq.tms.tms_microservice.exception;

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
}
