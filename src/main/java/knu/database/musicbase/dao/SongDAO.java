package knu.database.musicbase.dao;

import knu.database.musicbase.data.Song;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class SongDAO extends BasicDataAccessObjectImpl<Song, Long> {

    @Override
    public Song save(Song entity) {
        // Implementation commented out in original file, keeping it as is or returning
        // null as per original.
        return null;
    }

    @Override
    public Optional<Song> findById(Long id) {
        // Implementation commented out in original file.
        return Optional.empty();
    }

    @Override
    public List<Song> findAll() {
        // Implementation commented out in original file.
        return List.of();
    }

    /**
     * 특정 아티스트의 모든 음원을 제목 기준 오름차순으로 정렬하여 반환
     * (query 8.2)
     * 
     * @param artistName 아티스트 이름
     * @return Song 리스트
     */
    public List<Song> findByArtist(String artistName) {
        String sql = "SELECT S.* " +
                "FROM SONGS S " +
                "JOIN MADE_BY M ON S.Song_id = M.Song_id " +
                "JOIN ARTISTS A ON M.Artist_id = A.Artist_id " +
                "WHERE A.Name = ? " +
                "ORDER BY S.Title ASC";

        return executeQuery(sql, this::mapToSong, artistName);
    }

    private Song mapToSong(ResultSet rs) throws SQLException {
        return new Song(
                rs.getLong("Song_id"),
                rs.getString("Title"),
                rs.getLong("Length"),
                rs.getString("Play_link"),
                rs.getTimestamp("Create_at"),
                "");
    }

    /**
     * 9 번 유형 쿼리 활용 (집계함수 활용)
     */
    public int countSongs(String title, boolean titleExact, String artist, boolean artistExact,
            Integer lenMin, Integer lenMax, String provider, boolean providerExact,
            String dateMin, String dateMax) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT s.Song_id) AS total_count " +
                        "FROM SONGS s " +
                        "LEFT JOIN PROVIDERS p ON s.Provider_id = p.Provider_id " +
                        "LEFT JOIN MADE_BY mb ON s.Song_id = mb.Song_id " +
                        "LEFT JOIN ARTISTS a ON mb.Artist_id = a.Artist_id");

        List<String> whereConditions = new ArrayList<>();
        if (title != null) {
            whereConditions.add("UPPER(s.Title) " + (titleExact ? "= ?" : "LIKE ?"));
            params.add(titleExact ? title.toUpperCase() : "%" + title.toUpperCase() + "%");
        }
        if (artist != null) {
            whereConditions.add("UPPER(a.Name) " + (artistExact ? "= ?" : "LIKE ?"));
            params.add(artistExact ? artist.toUpperCase() : "%" + artist.toUpperCase() + "%");
        }
        if (provider != null) {
            whereConditions.add("UPPER(p.Provider_name) " + (providerExact ? "= ?" : "LIKE ?"));
            params.add(providerExact ? provider.toUpperCase() : "%" + provider.toUpperCase() + "%");
        }
        if (lenMin != null) {
            whereConditions.add("s.Length >= ?");
            params.add(lenMin);
        }
        if (lenMax != null) {
            whereConditions.add("s.Length <= ?");
            params.add(lenMax);
        }
        if (dateMin != null) {
            whereConditions.add("s.Create_at >= TO_DATE(?, 'YYYY-MM-DD')");
            params.add(dateMin);
        }
        if (dateMax != null) {
            whereConditions.add("s.Create_at <= TO_DATE(?, 'YYYY-MM-DD')");
            params.add(dateMax);
        }
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }

        return executeCount(sql.toString(), params.toArray());
    }

    public List<Song> searchSongs(String title, boolean titleExact, String artist, boolean artistExact,
            Integer lenMin, Integer lenMax, String provider, boolean providerExact,
            String dateMin, String dateMax, String orderBy, String orderDir) {

        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT s.Song_id, s.Title, s.Length, s.Play_link, s.Create_at, p.Provider_name " +
                        "FROM SONGS s " +
                        "LEFT JOIN PROVIDERS p ON s.Provider_id = p.Provider_id " +
                        "LEFT JOIN MADE_BY mb ON s.Song_id = mb.Song_id " +
                        "LEFT JOIN ARTISTS a ON mb.Artist_id = a.Artist_id");

        List<String> whereConditions = new ArrayList<>();
        if (title != null) {
            whereConditions.add("UPPER(s.Title) " + (titleExact ? "= ?" : "LIKE ?"));
            params.add(titleExact ? title.toUpperCase() : "%" + title.toUpperCase() + "%");
        }
        if (artist != null) {
            whereConditions.add("UPPER(a.Name) " + (artistExact ? "= ?" : "LIKE ?"));
            params.add(artistExact ? artist.toUpperCase() : "%" + artist.toUpperCase() + "%");
        }
        if (provider != null) {
            whereConditions.add("UPPER(p.Provider_name) " + (providerExact ? "= ?" : "LIKE ?"));
            params.add(providerExact ? provider.toUpperCase() : "%" + provider.toUpperCase() + "%");
        }
        if (lenMin != null) {
            whereConditions.add("s.Length >= ?");
            params.add(lenMin);
        }
        if (lenMax != null) {
            whereConditions.add("s.Length <= ?");
            params.add(lenMax);
        }
        if (dateMin != null) {
            whereConditions.add("s.Create_at >= TO_DATE(?, 'YYYY-MM-DD')");
            params.add(dateMin);
        }
        if (dateMax != null) {
            whereConditions.add("s.Create_at <= TO_DATE(?, 'YYYY-MM-DD')");
            params.add(dateMax);
        }
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }

        if (orderBy != null && orderDir != null) {
            sql.append(" ORDER BY ").append(orderBy).append(" ").append(orderDir);
        }

        return executeQuery(sql.toString(), this::mapResultSetToSong, params.toArray());
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
                rs.getString("Provider_name"));
    }
}