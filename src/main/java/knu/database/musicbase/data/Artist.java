package knu.database.musicbase.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Artist {
    private long id;
    private String name;
    private String gender;

    public Artist(String name, String gender) {
        this.id = -1;
        this.name = name;
        this.gender = gender;
    }
}
