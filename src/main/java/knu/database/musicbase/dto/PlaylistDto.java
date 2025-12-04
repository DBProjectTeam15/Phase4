package knu.database.musicbase.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaylistDto {
    private long id;
    private String title;
    private String isCollaborative;
    private long ownerId;
}
