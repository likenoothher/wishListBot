package data;

public class NotFoundUserNameException extends Exception{
    public NotFoundUserNameException(String message) {
        super(message);
    }
}
