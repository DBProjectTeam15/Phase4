package knu.database.musicbase.dao;

import knu.database.musicbase.data.Playlist;
import knu.database.musicbase.data.SongDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PlaylistDAO extends BasicDataAccessObjectImpl<Playlist, Long> {

    @Override
    public Playlist save(Playlist entity) throws SQLException {
        String sql = "INSERT INTO PLAYLISTS (Title, Is_collaborative, User_id) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"Playlist_id"})) {

            pstmt.setString(1, entity.getTitle());
            pstmt.setString(2, entity.getIsCollaborative());
            pstmt.setLong(3, entity.getUserId()); // DTO에 userId가 포함됨

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating playlist failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long generatedId = generatedKeys.getLong(1);
                    return new Playlist(generatedId, entity.getTitle(), entity.getIsCollaborative(), entity.getUserId());
                } else {
                    throw new SQLException("Creating playlist failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public Optional<Playlist> findById(Long id) {
        String sql = "SELECT * FROM PLAYLISTS WHERE Playlist_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPlaylist(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Playlist> findAll() {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT * FROM PLAYLISTS";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                playlists.add(mapResultSetToPlaylist(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playlists;
    }


    /**
     * 쿼리 9.2 활용
     * 수정 사항 : ROWNUM을 사용하기 위해 ORDER BY를 포함한 서브쿼리로 감쌉니다.
     */
    public List<Playlist> top10BySongCountOrderByDESCAndSet() {
        List<Playlist> playlists = new ArrayList<>();

        String sql = "SELECT * FROM ( " +
                "  SELECT P.Playlist_id, P.Title, P.Is_collaborative, P.User_id, COUNT(C.Song_id) AS Song_Count " +
                "  FROM PLAYLISTS P " +
                "  LEFT JOIN CONSISTED_OF C ON P.Playlist_id = C.Playlist_id " +
                "  GROUP BY P.Playlist_id, P.Title, P.Is_collaborative, P.User_id " +
                "  ORDER BY Song_Count DESC " +
                ") P_SORTED " +
                "WHERE ROWNUM <= 10";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                playlists.add(mapResultSetToPlaylist(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playlists;
    }

    // 쿼리 10.3 활용
    public List<Playlist> findEditable(long userId) {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT Playlist_id, Title, Is_collaborative, User_id FROM PLAYLISTS WHERE User_id = ? " +
                "UNION " +
                "SELECT P.Playlist_id, P.Title, P.Is_collaborative, P.User_id " +
                "FROM PLAYLISTS P " +
                "JOIN EDITS E ON P.Playlist_id = E.Playlist_id " +
                "WHERE E.User_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setLong(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    playlists.add(mapResultSetToPlaylist(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playlists;
    }


    // 쿼리 1.4 활용 - 특정 유저가 "소유한" 플레이리스트 조회
    // "SELECT * FROM PLAYLISTS WHERE User_id = ?"
    //
    // 쿼리 1.4
    // SELECT Playlist_id, Title
    // FROM PLAYLISTS
    // WHERE User_id = 10000001;
    //
    // 변경사항 : PLAYLISTS 테이블 전체 컬럼 정보 받아오도록 변경
    public List<Playlist> findByUserId(long userId) {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT * FROM PLAYLISTS WHERE User_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    playlists.add(mapResultSetToPlaylist(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playlists;
    }

    /**
     * 쿼리 2.1 활용 -> 변경 했습니다.
     *
     * 기존 : SELECT S.Title, S.Play_link
     * FROM PLAYLISTS P, CONSISTED_OF C, SONGS S
     * WHERE P.Playlist_id = 10000021
     *   AND P.Playlist_id = C.Playlist_id
     *   AND C.Song_id = S.Song_id;
     *
     *
     *
     * 변경 : "SELECT S.SONG_ID, S.TITLE AS SONG_TITLE, S.PLAY_LINK, A.NAME AS ARTIST_NAME " +
     *                          "FROM CONSISTED_OF CO " +
     *                          "JOIN SONGS S ON CO.SONG_ID = S.SONG_ID " +
     *                          "LEFT JOIN MADE_BY MB ON S.SONG_ID = MB.SONG_ID AND MB.ROLE = 'Singer' " +
     *                          "LEFT JOIN ARTISTS A ON MB.ARTIST_ID = A.ARTIST_ID " +
     *                          "WHERE CO.PLAYLIST_ID = ?"
     * 변경사항 :
     *      테이블 추가 및 명시적 조인으로 PLAYLISTS 테이블 대신 CONSISTED_OF를 시작으로, MADE_BY, ARTISTS 테이블을
     *      LEFT JOIN으로 추가하여 곡의 아티스트 이름을 함께 가져옵니다.
     *      출력 항목을 추가해 SONG_ID와 아티스트 이름(ARTIST_NAME)을 추가로 조회합니다.
     */
    public List<SongDetail> findPlaylistDetailsByPlaylistId(long playlistId) {
        String sql = "SELECT S.SONG_ID, S.TITLE AS SONG_TITLE, S.PLAY_LINK, A.NAME AS ARTIST_NAME " +
                "FROM CONSISTED_OF CO " +
                "JOIN SONGS S ON CO.SONG_ID = S.SONG_ID " +
                "LEFT JOIN MADE_BY MB ON S.SONG_ID = MB.SONG_ID AND MB.ROLE = 'Singer' " +
                "LEFT JOIN ARTISTS A ON MB.ARTIST_ID = A.ARTIST_ID " +
                "WHERE CO.PLAYLIST_ID = ?";

        List<SongDetail> songDetails = new ArrayList<>();

        try (Connection connection = getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, playlistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songDetails.add(
                            new SongDetail(
                                    rs.getLong("SONG_ID"),
                                    rs.getString("SONG_TITLE"),
                                    rs.getString("PLAY_LINK"),
                                    rs.getString("ARTIST_NAME")
                            )
                    );

                }
            }
            return songDetails;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    //4. getSharedPlaylists : 쿼리 2.3
    //SELECT P.Playlist_id, P.Title, P.User_id AS Owner_User_id
    //FROM PLAYLISTS P, EDITS E
    //WHERE E.User_id = 10000002
    //  AND E.Playlist_id = P.Playlist_id;
    //==================================================
    //"SELECT P.PLAYLIST_ID, P.TITLE, P.IS_COLLABORATIVE, U.USER_ID AS OWNER_ID " +
    //                         "FROM PLAYLISTS P " +
    //                         "JOIN USERS U ON P.USER_ID = U.USER_ID " +
    //                         "JOIN EDITS E ON P.PLAYLIST_ID = E.PLAYLIST_ID " +
    //                         "WHERE E.USER_ID = ? AND P.USER_ID != ?";
    // 변경사항 :
    //      테이블 추가 및 명시적 조인으로 USERS U 테이블을 추가하고 명시적인 JOIN 키워드를 사용하여 소유자의 닉네임을 가져옵니다.
    //      조건을 추가해 편집 권한이 있는 유저 조건에 그 플레이리스트의 소유자는 아닐 것 (P.USER_ID != ?)이라는 조건이 추가되었습니다.
    public List<Playlist> findSharedPlaylistsByUserId(long userId) {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT P.PLAYLIST_ID, P.TITLE, P.IS_COLLABORATIVE, P.USER_ID " +
                "FROM PLAYLISTS P " +
                "JOIN USERS U ON P.USER_ID = U.USER_ID " +
                "JOIN EDITS E ON P.PLAYLIST_ID = E.PLAYLIST_ID " +
                "WHERE E.USER_ID = ? AND P.USER_ID != ?";

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setLong(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    playlists.add(mapResultSetToPlaylist(rs));
                }
            }
            return playlists;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

    }

    public List<Playlist> searchPlaylists(String title, boolean titleExact,
                                          Integer songCountMin, Integer songCountMax,
                                          Integer commentCountMin, Integer commentCountMax,
                                          String owner, boolean ownerExact,
                                          Integer lenMin, Integer lenMax) {

        List<Playlist> playlists = new ArrayList<>();
        List<Object> params = new ArrayList<>(); // PreparedStatement에 바인딩할 파라미터 리스트

        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "  p.Playlist_id, p.Title, p.Is_collaborative, p.User_id, " +
                        "  COUNT(DISTINCT co.Song_id) AS SongCount, " +
                        "  COUNT(DISTINCT c.Comment_id) AS CommentCount, " +
                        "  NVL(SUM(s.Length), 0) AS TotalLength " +
                        "FROM PLAYLISTS p " +
                        "LEFT JOIN USERS u ON p.User_id = u.User_id " +
                        "LEFT JOIN CONSISTED_OF co ON p.Playlist_id = co.Playlist_id " +
                        "LEFT JOIN SONGS s ON co.Song_id = s.Song_id " +
                        "LEFT JOIN COMMENTS c ON p.Playlist_id = c.Playlist_id "
        );

        // 2. 동적 WHERE 절 (DBManager 로직)
        List<String> whereConditions = new ArrayList<>();
        if (title != null) {
            whereConditions.add("UPPER(p.Title) " + (titleExact ? "= ?" : "LIKE ?"));
            params.add(titleExact ? title.toUpperCase() : "%" + title.toUpperCase() + "%");
        }
        if (owner != null) {
            whereConditions.add("UPPER(u.Nickname) " + (ownerExact ? "= ?" : "LIKE ?"));
            params.add(ownerExact ? owner.toUpperCase() : "%" + owner.toUpperCase() + "%");
        }
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }

        // 3. GROUP BY 절 (mapResultSetToPlaylist를 위해 SELECT한 컬럼들 추가)
        sql.append(" GROUP BY p.Playlist_id, p.Title, p.Is_collaborative, p.User_id, u.Nickname ");

        // 4. 동적 HAVING 절 (DBManager 로직)
        List<String> havingConditions = new ArrayList<>();
        if (songCountMin != null) { havingConditions.add("COUNT(DISTINCT co.Song_id) >= ?"); params.add(songCountMin); }
        if (songCountMax != null) { havingConditions.add("COUNT(DISTINCT co.Song_id) <= ?"); params.add(songCountMax); }
        if (commentCountMin != null) { havingConditions.add("COUNT(DISTINCT c.Comment_id) >= ?"); params.add(commentCountMin); }
        if (commentCountMax != null) { havingConditions.add("COUNT(DISTINCT c.Comment_id) <= ?"); params.add(commentCountMax); }
        if (lenMin != null) { havingConditions.add("NVL(SUM(s.Length), 0) >= ?"); params.add(lenMin); }
        if (lenMax != null) { havingConditions.add("NVL(SUM(s.Length), 0) <= ?"); params.add(lenMax); }
        if (!havingConditions.isEmpty()) {
            sql.append(" HAVING ").append(String.join(" AND ", havingConditions));
        }

        // 5. 정렬 (DBManager 로직)
        sql.append(" ORDER BY p.Title ASC");

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // 파라미터 바인딩
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    playlists.add(mapResultSetToPlaylist(rs)); // 기존 매퍼 재사용
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // 예외 처리
        }

        return playlists;
    }


    private Playlist mapResultSetToPlaylist(ResultSet rs) throws SQLException {
        return new Playlist(
                rs.getLong("Playlist_id"),
                rs.getString("Title"),
                rs.getString("IS_COLLABORATIVE"),
                rs.getLong("User_id")
        );
    }
}