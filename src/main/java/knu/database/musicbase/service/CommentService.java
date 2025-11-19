package knu.database.musicbase.service;

import knu.database.musicbase.dao.CommentDAO;
import knu.database.musicbase.data.Comment;
import knu.database.musicbase.data.CommentWithAuthor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 현재는 상태를 저장하기 위한 기능만을 수행합니다.
 */
@RequiredArgsConstructor
public class CommentService {
    @Getter
    private List<Comment> comments;
    @Getter
    private String title;
    @Getter
    private List<CommentWithAuthor> commentsWithAuthor;

    private final CommentDAO commentDAO;

    public void findByUserId(long userId) {
        this.comments = commentDAO.findByUserId(userId);
        this.title = "내가 작성한 댓글 목록";
    }

    public void findByPlaylistIdWithAuthor(long playlistId) {
        this.commentsWithAuthor = commentDAO.findByPlaylistWithAuthor(playlistId);
        this.title = "플레이리스트의 댓글 목록";
    }
}
