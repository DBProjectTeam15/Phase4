package knu.database.musebase.controller.my;

import knu.database.musebase.console.PageController;
import knu.database.musebase.console.PageKey;
import knu.database.musebase.data.Comment;
import knu.database.musebase.exception.InvalidLoginStateException;
import knu.database.musebase.service.CommentService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MyCommentController implements PageController<PageKey> {

    private final CommentService commentService;

    @Override
    public void displayScreen() throws InvalidLoginStateException {
        System.out.println("-- " + commentService.getTitle() + " --");
        System.out.println("ID : 플레이리스트 ID : 내용 : 일시");
        for (Comment c : commentService.getComments()) {
            System.out.println(c.getId().getId() + " : " + c.getId().getPlaylistId() + " : " + c.getContent() + " : " + c.getCommentedAt());
        }

        System.out.println("\n0. 돌아가기");
    }

    @Override
    public PageKey invoke(String[] commands) {
        return switch (commands[0]) {
            case "0" -> PageKey.MY_PAGE;
            default -> PageKey.MY_PAGE_COMMENT;
        };
    }
}
