package knu.database.musicbase.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private LocalDateTime commentedAt;
    private Long userId;
    private Long playlistId;
    private String content;
}
