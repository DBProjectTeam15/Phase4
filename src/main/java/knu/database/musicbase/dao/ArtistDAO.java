package knu.database.musicbase.dao;

import knu.database.musicbase.data.Artist;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CREATE TABLE ARTISTS (
 * Artist_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 * Name VARCHAR2(30) NOT NULL,
 * Gender VARCHAR2(10)
 * );
 */
@Slf4j
public class ArtistDAO extends BasicDataAccessObjectImpl<Artist, Long> {

    @Override
    public Artist save(Artist entity) {
        String sql = "INSERT INTO ARTISTS (Name, Gender) VALUES (?, ?)";

        try (Connection connection = getConnection();

             // Oracle은 이렇게 해야 가져올 수 생성된 키 값을 가져올 수 있음.
             PreparedStatement pstmt = connection.prepareStatement(sql, new String[] { "Artist_id" })) {

            pstmt.setString(1, entity.getName());
            pstmt.setString(2, entity.getGender());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating artist failed, no rows affected.");
            }

            // 생성된 키 (Artist_id)를 가져옴
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1); // 생성된 Artist_id

                    return new Artist(id, entity.getName(), entity.getGender());
                } else {
                    throw new SQLException("Creating artist failed, no ID obtained.");
                }
            }

        } catch (SQLException ex) {
            log.error("Error saving artist: " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public Optional<Artist> findById(Long id) {
        String sql = "SELECT * FROM ARTISTS WHERE Artist_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToArtist(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException ex) {
            log.error("Error finding artist by id {}: {}", id, ex.getMessage(), ex);
            return Optional.empty();
        }
    }



    @Override
    public List<Artist> findAll() {
        String sql = "SELECT * FROM ARTISTS";
        List<Artist> artists = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                artists.add(mapToArtist(rs));
            }

        } catch (SQLException ex) {
            log.error("Error finding all artists: " + ex.getMessage(), ex);
        }
        return artists;
    }

    public long deleteById(long id) {
        String sql = "DELETE FROM ARTISTS WHERE Artist_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            // executeUpdate()는 영향을 받은 행의 수를 반환합니다.
            // ID로 삭제하는 경우 0 (삭제 실패) 또는 1 (삭제 성공)이 됩니다.
            return pstmt.executeUpdate();

        } catch (SQLException ex) {
            log.error("Error deleting artist by id {}: {}", id, ex.getMessage(), ex);
            // 오류 발생 시 0을 반환
            return 0;
        }
    }

    /**
     * 집계 함수 사용 - 쿼리 3번 / 2번 유형 활용
     * @param name
     * @param nameExact
     * @param gender
     * @param roles
     * @return
     */
    public int countArtists(String name, boolean nameExact, String gender, List<String> roles) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT a.Artist_id) AS total_count FROM ARTISTS a ");
        List<String> whereConditions = new ArrayList<>();

        if (roles != null && !roles.isEmpty()) {
            sql.append("LEFT JOIN ART_TYPES at ON a.Artist_id = at.Artist_id ");
            List<String> upperRoles = roles.stream().map(String::toUpperCase).collect(Collectors.toList());
            String placeholders = upperRoles.stream().map(r -> "?").collect(Collectors.joining(", "));
            whereConditions.add("UPPER(at.Artist_type) IN (" + placeholders + ")");
            params.addAll(upperRoles);
        }
        if (name != null) {
            whereConditions.add("UPPER(a.Name) " + (nameExact ? "= ?" : "LIKE ?"));
            params.add(nameExact ? name.toUpperCase() : "%" + name.toUpperCase() + "%");
        }
        if (gender != null) {
            if (gender.equals("None")) {
                whereConditions.add("a.Gender IS NULL");
            } else {
                whereConditions.add("a.Gender = ?");
                params.add(gender);
            }
        }
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }

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

    /**
     * 유형 6번 쿼리를 응용 했습니다. (IN절)
     *
     * SELECT S.Title, S.Play_link
     * FROM SONGS S
     * JOIN PROVIDERS P ON S.Provider_id = P.Provider_id
     * WHERE P.Provider_name IN ('Youtube_music', 'Sound_cloud');
     */
    public List<Artist> searchArtists(String name, boolean nameExact, String gender, List<String> roles) {
        List<Artist> artists = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        // DTO 매핑을 위해 'DisplayName' 대신 'a.Name'을 직접 SELECT
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT a.Artist_id, a.Name, a.Gender " +
                        "FROM ARTISTS a "
        );

        List<String> whereConditions = new ArrayList<>();
        if (roles != null && !roles.isEmpty()) {
            sql.append("LEFT JOIN ART_TYPES at ON a.Artist_id = at.Artist_id ");
            List<String> upperRoles = roles.stream().map(String::toUpperCase).collect(Collectors.toList());
            String placeholders = upperRoles.stream().map(r -> "?").collect(Collectors.joining(", "));
            whereConditions.add("UPPER(at.Artist_type) IN (" + placeholders + ")"); // IN 활용된 부분
            params.addAll(upperRoles);
        }
        if (name != null) {
            whereConditions.add("UPPER(a.Name) " + (nameExact ? "= ?" : "LIKE ?"));
            params.add(nameExact ? name.toUpperCase() : "%" + name.toUpperCase() + "%");
        }
        if (gender != null) {
            if (gender.equals("None")) {
                whereConditions.add("a.Gender IS NULL");
            } else {
                whereConditions.add("a.Gender = ?");
                params.add(gender);
            }
        }
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }

        sql.append(" ORDER BY a.Name ASC"); // 정렬

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    artists.add(mapToArtist(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return artists;
    }

    /**
     * ResultSet -> Artist 객체 매핑 헬퍼
     */
    private Artist mapToArtist(ResultSet rs) throws SQLException {
        return new Artist(
                rs.getLong("Artist_id"),
                rs.getString("Name"),
                rs.getString("Gender")
        );
    }
}