package knu.database.musicbase.controller.my;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.data.Comment;
import knu.database.musicbase.exception.InvalidLoginStateException;
import knu.database.musicbase.service.CommentService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MyCommentController implements PageController<PageKey> {

    private final CommentService commentService;

    @Override
    public void displayScreen() throws InvalidLoginStateException {
        System.out.println("-- " + commentService.getTitle() + " --");
        System.out.println("ID : 플레이리스트 ID : 내용 : 일시");
        for (Comment c : commentService.getComments()) {
            System.out.println(c.getId().getCommentedAt() + " : " + c.getId().getPlaylistId() + " : " + c.getContent() + " : ");
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
