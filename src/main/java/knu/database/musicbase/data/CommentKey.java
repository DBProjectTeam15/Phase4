package knu.database.musicbase.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.sql.Timestamp;

@AllArgsConstructor
@Getter
public class CommentKey implements Serializable {
    private Timestamp commentedAt;
    private Long userId;
    private Long playlistId;
}
