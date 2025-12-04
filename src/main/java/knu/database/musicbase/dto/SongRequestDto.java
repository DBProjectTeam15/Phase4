package knu.database.musicbase.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SongRequestDto {
    private long id;
    private String title;
    private long artist;
    private long requestUserId;
    private LocalDateTime requestAt;
}
