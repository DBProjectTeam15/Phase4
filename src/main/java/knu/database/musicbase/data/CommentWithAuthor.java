package knu.database.musicbase.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class CommentWithAuthor {

    private String author;
    private String content;
    private Timestamp commentedAt;
}
