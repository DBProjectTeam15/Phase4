package knu.database.musicbase.console;

public enum ConsoleMode {
    MANAGER,
    MAIN;

    public String toLowerCase() {
        return this.name().toLowerCase();
    }
}
