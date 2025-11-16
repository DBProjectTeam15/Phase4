package knu.database.musebase.dao;

import knu.database.musebase.data.Song;
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
        String sql = "INSERT INTO SONGS (Title, Length, Play_link, Create_at, Provider_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();

             PreparedStatement pstmt = connection.prepareStatement(sql, new String[] { "Song_id" })) {

            pstmt.setString(1, entity.getTitle());
            pstmt.setLong(2, entity.getLength());
            pstmt.setString(3, entity.getPlayLink());
            pstmt.setTimestamp(4, entity.getCreateAt()); // entity 생성 시 이미 설정됨
            pstmt.setLong(5, entity.getProviderId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating song failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);

                    return new Song(
                            id,
                            entity.getTitle(),
                            entity.getLength(),
                            entity.getPlayLink(),
                            entity.getCreateAt(),
                            entity.getProviderId()
                    );
                } else {
                    throw new SQLException("Creating song failed, no ID obtained.");
                }
            }
        } catch (SQLException ex) {
            log.error("Error saving song: " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public Optional<Song> findById(Long id) {
        String sql = "SELECT * FROM SONGS WHERE Song_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // mapToSong 헬퍼 메서드를 사용하여 객체 매핑
                    return Optional.of(mapToSong(rs));
                }
            }
        } catch (SQLException ex) {
            log.error("Error finding song by id {}: {}", id, ex.getMessage(), ex);
        }
        return Optional.empty(); // 찾지 못하거나 오류 발생 시
    }

    @Override
    public List<Song> findAll() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM SONGS";

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                songs.add(mapToSong(rs));
            }
        } catch (SQLException ex) {
            log.error("Error finding all songs: " + ex.getMessage(), ex);
        }
        return songs;
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
                rs.getLong("Provider_id")
        );
    }
}