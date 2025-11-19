package knu.database.musicbase.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

//CREATE TABLE COMMENTS (
//		Comment_id NUMBER NOT NULL,
//		Content VARCHAR2(200) NOT NULL,
//		Commented_at TIMESTAMP NOT NULL,
//		User_id NUMBER NOT NULL REFERENCES USERS(User_id),
//		Playlist_id NUMBER NOT NULL REFERENCES PLAYLISTS(Playlist_id),
//		PRIMARY KEY (User_id, Playlist_id, Comment_id)
//);
@Getter
@AllArgsConstructor
public class Comment {

    private CommentKey id;
    private String content;
    private Timestamp commentedAt;



}
