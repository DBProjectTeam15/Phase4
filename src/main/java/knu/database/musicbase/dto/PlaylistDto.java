package knu.database.musicbase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PlaylistDto {
    private long id;
    private String title;
    private String isCollaborative;
    private long ownerId;
}
