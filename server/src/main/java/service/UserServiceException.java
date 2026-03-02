package service;

public class UserServiceException extends Exception{
    public BadRequestException(String message) {super(message);}
    public UnauthorizedException(String message) {super(message);}
    public AlreadyTakenException(String message) {super(message);}
    public ForbiddenException(String message) {super(message);}
}
