package knu.database.musicbase.dao;

import knu.database.musicbase.data.Song;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CREATE TABLE SONGS (
 * Song_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 * Title VARCHAR2(120) NOT NULL,
 * Length NUMBER NOT NULL,
 * Play_link VARCHAR2(255) NOT NULL,
 * Create_at TIMESTAMP,
 * Provider_id NUMBER NOT NULL REFERENCES PROVIDERS(Provider_id)
 * );
 */
@Slf4j // 로그 사용을 위해 어노테이션 추가
public class SongDAO extends BasicDataAccessObjectImpl<Song, Long> {

    @Override
    public Song save(Song entity) {
//        String sql = "INSERT INTO SONGS (Title, Length, Play_link, Create_at, Provider_id) VALUES (?, ?, ?, ?, ?)";
//
//        try (Connection connection = getConnection();
//
//             PreparedStatement pstmt = connection.prepareStatement(sql, new String[] { "Song_id" })) {
//
//            pstmt.setString(1, entity.getTitle());
//            pstmt.setLong(2, entity.getLength());
//            pstmt.setString(3, entity.getPlayLink());
//            pstmt.setTimestamp(4, entity.getCreateAt()); // entity 생성 시 이미 설정됨
//            pstmt.setLong(5, entity.getProviderId());
//
//            int affectedRows = pstmt.executeUpdate();
//
//            if (affectedRows == 0) {
//                throw new SQLException("Creating song failed, no rows affected.");
//            }
//
//            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
//                if (generatedKeys.next()) {
//                    long id = generatedKeys.getLong(1);
//
//                    return new Song(
//                            id,
//                            entity.getTitle(),
//                            entity.getLength(),
//                            entity.getPlayLink(),
//                            entity.getCreateAt(),
//                            ""
//                    );
//                } else {
//                    throw new SQLException("Creating song failed, no ID obtained.");
//                }
//            }
//        } catch (SQLException ex) {
//            log.error("Error saving song: " + ex.getMessage(), ex);
//            return null;
//        }
        return null;
    }

    @Override
    public Optional<Song> findById(Long id) {
//        String sql = "SELECT * FROM SONGS WHERE Song_id = ?";
//
//        try (Connection connection = getConnection();
//             PreparedStatement pstmt = connection.prepareStatement(sql)) {
//
//            pstmt.setLong(1, id);
//
//            try (ResultSet rs = pstmt.executeQuery()) {
//                if (rs.next()) {
//                    // mapToSong 헬퍼 메서드를 사용하여 객체 매핑
//                    return Optional.of(mapToSong(rs));
//                }
//            }
//        } catch (SQLException ex) {
//            log.error("Error finding song by id {}: {}", id, ex.getMessage(), ex);
//        }
        return Optional.empty(); // 찾지 못하거나 오류 발생 시
    }

    @Override
    public List<Song> findAll() {
//        List<Song> songs = new ArrayList<>();
//        String sql = "SELECT * FROM SONGS";
//
//        try (Connection connection = getConnection();
//             PreparedStatement pstmt = connection.prepareStatement(sql);
//             ResultSet rs = pstmt.executeQuery()) {
//
//            while (rs.next()) {
//                songs.add(mapToSong(rs));
//            }
//        } catch (SQLException ex) {
//            log.error("Error finding all songs: " + ex.getMessage(), ex);
//        }
//        return songs;
        return List.of();
    }

    /**
     * 특정 아티스트의 모든 음원을 제목 기준 오름차순으로 정렬하여 반환
     * (query 8.2)
     * @param artistName 아티스트 이름
     * @return Song 리스트
     */
    public List<Song> findByArtist(String artistName) {
        List<Song> songs = new ArrayList<>();

        String sql = "SELECT S.* " +
                "FROM SONGS S " +
                "JOIN MADE_BY M ON S.Song_id = M.Song_id " +
                "JOIN ARTISTS A ON M.Artist_id = A.Artist_id " +
                "WHERE A.Name = ? " +
                "ORDER BY S.Title ASC";

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, artistName); // WHERE 절의 ? 에 아티스트 이름 바인딩

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapToSong(rs));
                }
            }
        } catch (SQLException ex) {
            log.error("Error finding songs by artist {}: {}", artistName, ex.getMessage(), ex);
        }
        return songs;
    }


    private Song mapToSong(ResultSet rs) throws SQLException {
        return new Song(
                rs.getLong("Song_id"),
                rs.getString("Title"),
                rs.getLong("Length"),
                rs.getString("Play_link"),
                rs.getTimestamp("Create_at"),
                ""
        );
    }

    /**
     * 9 번 유형 쿼리 활용 (집계함수 활용)
     *
     * @param title
     * @param titleExact
     * @param artist
     * @param artistExact
     * @param lenMin
     * @param lenMax
     * @param provider
     * @param providerExact
     * @param dateMin
     * @param dateMax
     * @return
     */
    public int countSongs(String title, boolean titleExact, String artist, boolean artistExact,
                          Integer lenMin, Integer lenMax, String provider, boolean providerExact,
                          String dateMin, String dateMax) {
        // (이전 응답의 코드와 동일)
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT s.Song_id) AS total_count " +
                        "FROM SONGS s " +
                        "LEFT JOIN PROVIDERS p ON s.Provider_id = p.Provider_id " +
                        "LEFT JOIN MADE_BY mb ON s.Song_id = mb.Song_id " +
                        "LEFT JOIN ARTISTS a ON mb.Artist_id = a.Artist_id"
        );

        List<String> whereConditions = new ArrayList<>();
        if (title != null) { whereConditions.add("UPPER(s.Title) " + (titleExact ? "= ?" : "LIKE ?")); params.add(titleExact ? title.toUpperCase() : "%" + title.toUpperCase() + "%"); }
        if (artist != null) { whereConditions.add("UPPER(a.Name) " + (artistExact ? "= ?" : "LIKE ?")); params.add(artistExact ? artist.toUpperCase() : "%" + artist.toUpperCase() + "%"); }
        if (provider != null) { whereConditions.add("UPPER(p.Provider_name) " + (providerExact ? "= ?" : "LIKE ?")); params.add(providerExact ? provider.toUpperCase() : "%" + provider.toUpperCase() + "%"); }
        if (lenMin != null) { whereConditions.add("s.Length >= ?"); params.add(lenMin); }
        if (lenMax != null) { whereConditions.add("s.Length <= ?"); params.add(lenMax); }
        if (dateMin != null) { whereConditions.add("s.Create_at >= TO_DATE(?, 'YYYY-MM-DD')"); params.add(dateMin); }
        if (dateMax != null) { whereConditions.add("s.Create_at <= TO_DATE(?, 'YYYY-MM-DD')"); params.add(dateMax); }
        if (!whereConditions.isEmpty()) { sql.append(" WHERE ").append(String.join(" AND ", whereConditions)); }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Song> searchSongs(String title, boolean titleExact, String artist, boolean artistExact,
                                  Integer lenMin, Integer lenMax, String provider, boolean providerExact,
                                  String dateMin, String dateMax, String orderBy, String orderDir) {

        List<Song> songs = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT s.Song_id, s.Title, s.Length, s.Play_link, s.Create_at, p.Provider_name " + // [수정됨]
                        "FROM SONGS s " +
                        "LEFT JOIN PROVIDERS p ON s.Provider_id = p.Provider_id " +
                        "LEFT JOIN MADE_BY mb ON s.Song_id = mb.Song_id " +
                        "LEFT JOIN ARTISTS a ON mb.Artist_id = a.Artist_id"
        );

        List<String> whereConditions = new ArrayList<>();
        if (title != null) { whereConditions.add("UPPER(s.Title) " + (titleExact ? "= ?" : "LIKE ?")); params.add(titleExact ? title.toUpperCase() : "%" + title.toUpperCase() + "%"); }
        if (artist != null) { whereConditions.add("UPPER(a.Name) " + (artistExact ? "= ?" : "LIKE ?")); params.add(artistExact ? artist.toUpperCase() : "%" + artist.toUpperCase() + "%"); }
        if (provider != null) { whereConditions.add("UPPER(p.Provider_name) " + (providerExact ? "= ?" : "LIKE ?")); params.add(providerExact ? provider.toUpperCase() : "%" + provider.toUpperCase() + "%"); }
        if (lenMin != null) { whereConditions.add("s.Length >= ?"); params.add(lenMin); }
        if (lenMax != null) { whereConditions.add("s.Length <= ?"); params.add(lenMax); }
        if (dateMin != null) { whereConditions.add("s.Create_at >= TO_DATE(?, 'YYYY-MM-DD')"); params.add(dateMin); }
        if (dateMax != null) { whereConditions.add("s.Create_at <= TO_DATE(?, 'YYYY-MM-DD')"); params.add(dateMax); }
        if (!whereConditions.isEmpty()) { sql.append(" WHERE ").append(String.join(" AND ", whereConditions)); }

        if (orderBy != null && orderDir != null) {
            sql.append(" ORDER BY ").append(orderBy).append(" ").append(orderDir);
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapResultSetToSong(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return songs;
    }

    /**
     * [수정됨] rs.getString("Play_link") 추가
     */
    private Song mapResultSetToSong(ResultSet rs) throws SQLException {
        return new Song(
                rs.getLong("Song_id"),
                rs.getString("Title"),
                rs.getInt("Length"),
                rs.getString("Play_link"), // [수정됨]
                rs.getTimestamp("Create_at"),
                rs.getString("Provider_name")
        );
    }
}