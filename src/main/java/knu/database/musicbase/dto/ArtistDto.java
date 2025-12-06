package knu.database.musicbase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ArtistDto {
    private long id;
    private String name;
    private String gender;
}
