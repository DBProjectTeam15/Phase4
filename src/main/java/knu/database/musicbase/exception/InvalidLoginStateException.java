package knu.database.musicbase.exception;

public class InvalidLoginStateException extends Exception {
    public InvalidLoginStateException() {
        super("Invalid login state");
    }
}
