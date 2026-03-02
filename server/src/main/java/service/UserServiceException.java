package service;

public class UserServiceException extends Exception{
    public AlreadyTakenException(String message) {super(message);}
    public ForbiddenException(String message) {super(message);}
}
