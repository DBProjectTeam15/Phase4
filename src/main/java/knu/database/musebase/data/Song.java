package knu.database.musebase.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class Song {
    private long id;
    private String title;
    private long length;
    private String playLink;
    private Timestamp createAt;
    private long providerId;

    public Song(String title, long length, String playLink, long providerId) {
        this.id = -1;
        this.title = title;
        this.length = length;
        this.playLink = playLink;
        this.createAt = new Timestamp(System.currentTimeMillis());
        this.providerId = providerId;
    }
}
