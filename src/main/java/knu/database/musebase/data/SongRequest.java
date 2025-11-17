package knu.database.musebase.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@AllArgsConstructor
@Getter
public class SongRequest {

    long id;
    String title;
    Timestamp requestAt;
    String artist;
    long userId;
    String managerUsername;
}
