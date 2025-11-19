package knu.database.musicbase.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

//Playlist_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
//                           Title VARCHAR2(30) NOT NULL,
//                           Is_collaborative VARCHAR2(10) NOT NULL,
//                           User_id NUMBER NOT NULL REFERENCES USERS(User_id)
@Getter
@AllArgsConstructor
public class Playlist {
    long id;
    String title;
    String isCollaborative;
    long userId;
}
