package knu.database.musebase.controller.search;

import knu.database.musebase.console.PageController;
import knu.database.musebase.console.PageKey;
import knu.database.musebase.exception.InvalidLoginStateException;

public class SearchController implements PageController<PageKey> {
    @Override
    public void displayScreen() throws InvalidLoginStateException {

    }

    @Override
    public PageKey invoke(String[] commands) {
        return null;
    }
}
