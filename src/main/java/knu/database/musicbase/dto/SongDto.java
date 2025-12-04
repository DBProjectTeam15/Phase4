package knu.database.musicbase.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SongDto {
    private long id;
    private String title;
    private String playLink;
    private String artistName;
}
