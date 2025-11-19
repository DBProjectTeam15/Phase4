package knu.database.musicbase.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SongDetail {
    private long id;
    private String title;
    private String playLink;
    private String artistName;
}
