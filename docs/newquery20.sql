-- 쿼리 1.1 (유저 기능): 특정 아이디를 가진 유저의 닉네임 조회

SELECT User_id, Nickname, Password, Email 
FROM USERS 
WHERE User_id = 10000001;

-- 쿼리 1.2 (음원 관리 기능): 허용된 음원 공급처 중 'Youtube' 의 정보 조회

SELECT Provider_id, Provider_link
FROM PROVIDERS
WHERE Provider_name = 'Youtube_music';

-- 쿼리 1.3 (음원 조회 기능): 제목에 'Rain'이라는 단어가 포함된 모든 음원 조회

SELECT Title, Play_link, Length
FROM SONGS
WHERE Title LIKE '%Go%';

-- 쿼리 1.4 (유저 기능): 특정 유저가 '소유한' 모든 플레이리스트의 제목 조회

SELECT *
FROM PLAYLISTS
WHERE User_id = 10000001;

-- 쿼리 2.1 (플레이리스트 기능): 특정 플레이리스트(ID: 101)에 포함된 모든 음원의 제목과 재생 링크 조회

SELECT S.SONG_ID, S.TITLE AS SONG_TITLE, S.PLAY_LINK, A.NAME AS ARTIST_NAME
FROM CONSISTED_OF CO
JOIN SONGS S
ON CO.SONG_ID = S.SONG_ID
LEFT JOIN MADE_BY MB
ON S.SONG_ID = MB.SONG_ID AND MB.ROLE = 'Singer'
LEFT JOIN ARTISTS A ON MB.ARTIST_ID = A.ARTIST_ID
WHERE CO.PLAYLIST_ID = 10000021;

-- 쿼리 2.2 (음원 조회 기능): 특정 아티스트(ID: 77)가 작업(모든 역할)한 음원의 제목과 역할 조회

SELECT S.Title, M.Role
FROM SONGS S, ARTISTS A, MADE_BY M
WHERE A.Artist_id = 10000080
  AND A.Artist_id = M.Artist_id
  AND M.Song_id = S.Song_id;

-- 쿼리 2.3 (플레이리스트 기능): 특정 유저(ID: 2)가 '편집 권한이 있는' 모든 플레이리스트의 제목과 소유자(유저 ID) 조회

/*
SELECT P.PLAYLIST_ID, P.TITLE, P.IS_COLLABORATIVE, P.USER_ID
FROM PLAYLISTS P
JOIN USERS U ON P.USER_ID = U.USER_ID
JOIN EDITS E ON P.PLAYLIST_ID = E.PLAYLIST_ID
WHERE E.USER_ID = ? AND P.USER_ID != ?
*/

-- 쿼리 3.1 (유저 기능): 각 유저(닉네임)가 소유한(생성한) 플레이리스트의 총 개수 집계

SELECT U.Nickname, COUNT(P.Playlist_id) AS Playlist_Count
FROM USERS U
JOIN PLAYLISTS P ON U.User_id = P.User_id
GROUP BY U.Nickname;

-- 쿼리 3.2 (음원 조회 기능): 각 아티스트(이름)가 작업에 참여한 음원의 총 개수 집계

SELECT A.Name, COUNT(M.Song_id) AS Song_Count
FROM ARTISTS A
JOIN MADE_BY M ON A.Artist_id = M.Artist_id
GROUP BY A.Name;

-- 쿼리 4.1 (음원 조회 기능): 'SoundCloud'에서 제공하는 모든 음원의 제목과 링크 조회 (WHERE 절 서브쿼리)

SELECT Title, Play_link
FROM SONGS
WHERE Provider_id = (SELECT Provider_id FROM PROVIDERS WHERE Provider_name = 'Sound_cloud');

-- 쿼리 4.2 (유저 기능): 한 번도 댓글을 작성하지 않은 유저의 닉네임 조회 (WHERE 절 서브쿼리, NOT IN)

SELECT Nickname
FROM USERS
WHERE User_id NOT IN (SELECT DISTINCT User_id FROM COMMENTS);

-- 쿼리 5.1 (유저 기능): 플레이리스트를 하나 이상 소유한(생성한) 유저의 닉네임과 이메일 조회

SELECT Nickname, Email
FROM USERS U
WHERE EXISTS (
    SELECT 1
    FROM PLAYLISTS P
    WHERE P.User_id = U.User_id
);

-- 쿼리 5.2 (음원 조회 기능): 서비스에 등록된 음원이 하나 이상 있는 아티스트의 이름 조회

SELECT Name
FROM ARTISTS A
WHERE EXISTS (
    SELECT 1
    FROM MADE_BY M
    WHERE M.Artist_id = A.Artist_id
);

-- 쿼리 6.1 (유저 기능): 'User\_Beta' 닉네임을 가진 유저가 소유한 모든 플레이리스트에 달린 댓글 내용 조회 (IN + 서브쿼리)

SELECT C.Content, C.Commented_at
FROM COMMENTS C
WHERE C.Playlist_id IN (
    SELECT P.Playlist_id
    FROM PLAYLISTS P
    JOIN USERS U ON P.User_id = U.User_id
    WHERE U.Nickname = 'nickname10'
);

-- 쿼리 6.2 (음원 관리 기능): 'Youtube' 또는 'SoundCloud' 공급처에서 제공하는 모든 음원 조회 (IN + 값 목록)

SELECT S.Title, S.Play_link
FROM SONGS S
JOIN PROVIDERS P ON S.Provider_id = P.Provider_id
WHERE P.Provider_name IN ('Youtube_music', 'Sound_cloud');

-- 쿼리 7.1 (플레이리스트 기능): 플레이리스트별 평균 음원 수 계산 (각 플레이리스트의 음원 수를 먼저 계산)

SELECT AVG(Song_Count) AS Avg_Songs_Per_Playlist
FROM (
    SELECT Playlist_id, COUNT(Song_id) AS Song_Count
    FROM CONSISTED_OF
    GROUP BY Playlist_id
) Playlist_Song_Counts;

-- 쿼리 7.2 (음원 관리 기능): 각 유저가 가장 최근에 요청한 음원 정보 조회 (유저별 최근 요청 시간을 먼저 계산)

SELECT U.Nickname, R_Stats.Request_song_title, R_Stats.Last_Request_Time
FROM USERS U
JOIN (
    SELECT User_id, Request_song_title, MAX(Request_at) AS Last_Request_Time
    FROM SONG_REQUESTS
    GROUP BY User_id, Request_song_title
) R_Stats ON U.User_id = R_Stats.User_id;

-- 쿼리 8.1 (유저 기능): 특정 플레이리스트(ID: 202)의 모든 댓글을 작성자 닉네임과 함께 최신순으로 정렬

SELECT U.Nickname, C.Content, C.Commented_at
FROM COMMENTS C
JOIN USERS U ON C.User_id = U.User_id
WHERE C.Playlist_id = 10000058
ORDER BY C.Commented_at DESC;

-- 쿼리 8.2 (음원 조회 기능): 특정 아티스트(이름: 'Artist\_Gamma')의 모든 음원을 제목 기준 오름차순으로 정렬

SELECT S.Title, S.Length, S.Play_link
FROM SONGS S
JOIN MADE_BY M ON S.Song_id = M.Song_id
JOIN ARTISTS A ON M.Artist_id = A.Artist_id
WHERE A.Name = 'Kenshi Yonezu'
ORDER BY S.Title ASC;

-- 쿼리 9.1 (유저 기능): 가장 많은 댓글을 작성한 유저 5명의 닉네임과 댓글 수를 내림차순으로 정렬

SELECT U.Nickname, COUNT(C.Comment_id) AS Comment_Count
FROM USERS U
JOIN COMMENTS C ON U.User_id = C.User_id
GROUP BY U.Nickname
ORDER BY Comment_Count DESC;

-- 쿼리 9.2 (플레이리스트 기능): 가장 많은 음원을 담고 있는 플레이리스트 10개의 제목과 음원 수를 내림차순으로 정렬

SELECT * FROM (
	SELECT
		P.Playlist_id,
		P.Title,
		P.Is_collaborative,
		P.User_id,
		COUNT(C.Song_id) AS Song_Count
	FROM PLAYLISTS P
	LEFT JOIN CONSISTED_OF C ON P.Playlist_id = C.Playlist_id
	GROUP BY P.Playlist_id, P.Title, P.Is_collaborative, P.User_id
  ORDER BY Song_Count DESC
) P_SORTED
WHERE ROWNUM <= 10;

-- 쿼리 10.1 (플레이리스트 기능): 특정 플레이리스트(ID: 303)의 소유자 또는 편집자인 모든 유저의 ID 조회 (UNION)

-- 소유자
SELECT User_id FROM PLAYLISTS WHERE Playlist_id = 10000058
UNION
-- 편집자
SELECT User_id FROM EDITS WHERE Playlist_id = 10000059;

-- 쿼리 10.2 (플레이리스트 기능): 플레이리스트 소유자이면서 동시에 (다른 플레이리스트의) 편집자이기도 한 유저의 ID 조회 (INTERSECT)

-- 소유자 목록 (중복 제거)
SELECT DISTINCT User_id FROM PLAYLISTS
INTERSECT
-- 편집자 목록 (중복 제거)
SELECT DISTINCT User_id FROM EDITS;

-- 쿼리 10.3 (유저 기능): 특정 유저(ID: 2)가 '소유했거나 편집 권한이 있는' 모든 플레이리스트의 ID와 제목 조회 (UNION)

-- 소유한 플레이리스트
SELECT Playlist_id, Title, Is_collaborative FROM PLAYLISTS WHERE User_id = 10000002
UNION
-- 편집 권한이 있는 플레이리스트
SELECT P.Playlist_id, P.Title, P.Is_collaborative
FROM PLAYLISTS P
JOIN EDITS E ON P.Playlist_id = E.Playlist_id
WHERE E.User_id = 10000002;

-- 추가 쿼리문

/*
SELECT COUNT(DISTINCT a.Artist_id) AS total_count 
FROM ARTISTS a 
LEFT JOIN ART_TYPES at ON a.Artist_id = at.Artist_id
WHERE UPPER(at.Artist_type) IN placeholders;
*/


SELECT COUNT(DISTINCT s.Song_id) AS total_count
FROM SONGS s
LEFT JOIN PROVIDERS p ON s.Provider_id = p.Provider_id
LEFT JOIN MADE_BY mb ON s.Song_id = mb.Song_id
LEFT JOIN ARTISTS a ON mb.Artist_id = a.Artist_id;


/*
SELECT DISTINCT a.Artist_id, a.Name, a.Gender
FROM ARTISTS a
LEFT JOIN ART_TYPES at ON a.Artist_id = at.Artist_id
WHERE UPPER(at.Artist_type) IN ( ? );
*/