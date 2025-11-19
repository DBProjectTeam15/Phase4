package knu.database.musicbase.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class CommentKey implements Serializable {
    private Long id;
    private Long userId;
    private Long playlistId;
}
