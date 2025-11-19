package knu.database.musicbase.console;

import knu.database.musicbase.exception.InvalidLoginStateException;

public interface PageController<T extends Enum<T>> {

    public void displayScreen() throws InvalidLoginStateException;

    public T invoke(String[] commands);
}
