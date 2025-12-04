-- DROP TABLE EDITS CASCADE CONSTRAINTS PURGE;
-- DROP TABLE ART_TYPES CASCADE CONSTRAINTS PURGE;
-- DROP TABLE CONSISTED_OF CASCADE CONSTRAINTS PURGE;
-- DROP TABLE COMMENTS CASCADE CONSTRAINTS PURGE;
-- DROP TABLE MADE_BY CASCADE CONSTRAINTS PURGE;
-- DROP TABLE ARTISTS CASCADE CONSTRAINTS PURGE;
-- DROP TABLE SONGS CASCADE CONSTRAINTS PURGE;
-- DROP TABLE PROVIDERS CASCADE CONSTRAINTS PURGE;
-- DROP TABLE SONG_REQUESTS CASCADE CONSTRAINTS PURGE;
-- DROP TABLE MANAGERS CASCADE CONSTRAINTS PURGE;
-- DROP TABLE PLAYLISTS CASCADE CONSTRAINTS PURGE;
-- DROP TABLE USERS CASCADE CONSTRAINTS PURGE;

CREATE TABLE USERS (
                       User_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 인조키에 AUTO GENERATED KEY 기능을 사용하게 하기 위해 변경했습니다.
                       Nickname VARCHAR2(30) NOT NULL,
                       Password VARCHAR2(255) NOT NULL,
                       Email VARCHAR2(50) NOT NULL UNIQUE -- 기능 구현 중, 이메일로 로그인하도록 기능을 수정했습니다.
);

CREATE TABLE MANAGERS (
                          Manager_id VARCHAR2(30) PRIMARY KEY,
                          Password VARCHAR2(255) NOT NULL,
                          Name VARCHAR2(30) NOT NULL
);

CREATE TABLE SONG_REQUESTS (
                               Request_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               Request_song_title VARCHAR2(30) NOT NULL,
                               Request_at TIMESTAMP NOT NULL,
                               Request_song_artist VARCHAR2(30) NOT NULL,
                               User_id NUMBER REFERENCES USERS(User_id) NOT NULL,
                               Manager_id VARCHAR2(30) REFERENCES MANAGERS(Manager_id)
);

CREATE TABLE PROVIDERS (
                           Provider_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           Provider_name VARCHAR2(30) NOT NULL,
                           Provider_link VARCHAR2(255) NOT NULL
);

CREATE TABLE SONGS (
                       Song_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       Title VARCHAR2(120) NOT NULL,
                       Length NUMBER NOT NULL,
                       Play_link VARCHAR2(255) NOT NULL,
                       Create_at TIMESTAMP,
                       Provider_id NUMBER NOT NULL REFERENCES PROVIDERS(Provider_id)
);

CREATE TABLE ARTISTS (
                         Artist_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         Name VARCHAR2(30) NOT NULL,
                         Gender VARCHAR2(10)
);

CREATE TABLE MADE_BY (
                         Song_id NUMBER NOT NULL REFERENCES SONGS(Song_id),
                         Artist_id NUMBER NOT NULL REFERENCES ARTISTS(Artist_id),
                         Role VARCHAR2(30) NOT NULL,
                         PRIMARY KEY(Song_id, Artist_id, Role)
);

CREATE TABLE PLAYLISTS (
                           Playlist_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, --
                           Title VARCHAR2(30) NOT NULL,
                           Is_collaborative VARCHAR2(10) NOT NULL,
                           User_id NUMBER NOT NULL REFERENCES USERS(User_id)
);

CREATE TABLE COMMENTS (
                          Content VARCHAR2(200) NOT NULL,
                          Commented_at TIMESTAMP NOT NULL,
                          User_id NUMBER NOT NULL REFERENCES USERS(User_id),
                          Playlist_id NUMBER NOT NULL REFERENCES PLAYLISTS(Playlist_id),
                          PRIMARY KEY (User_id, Playlist_id, Commented_at) -- 복합 키
);

CREATE TABLE CONSISTED_OF (
                              Playlist_id NUMBER NOT NULL REFERENCES PLAYLISTS(Playlist_id),
                              Song_id NUMBER NOT NULL REFERENCES SONGS(Song_id),
                              PRIMARY KEY (Playlist_id, Song_id)
);

CREATE TABLE ART_TYPES (
                           Artist_id NUMBER NOT NULL REFERENCES ARTISTS(Artist_id),
                           Artist_type VARCHAR2(30) NOT NULL,
                           PRIMARY KEY (Artist_type, Artist_id)
);

CREATE TABLE EDITS (
                       User_id NUMBER NOT NULL REFERENCES USERS(User_id),
                       Playlist_id NUMBER NOT NULL REFERENCES PLAYLISTS(Playlist_id),
                       PRIMARY KEY (User_id, Playlist_id)
);

COMMIT;

EXIT;