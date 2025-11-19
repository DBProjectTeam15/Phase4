package knu.database.musicbase.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Provider {
    private long id;
    private String name;
    private String link;

    public Provider(String name, String link) {
        this.id = -1;
        this.name = name;
        this.link = link;
    }
}
