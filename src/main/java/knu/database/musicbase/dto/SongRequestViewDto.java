package knu.database.musicbase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class SongRequestViewDto {
    private long id;
    private String RequestSongTitle;
    private String RequestSongArtist;
    private long UserId;
    private LocalDateTime requestAt;
}
