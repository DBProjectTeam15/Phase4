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
        // Note: executeUpdate doesn't support returning generated keys easily with the
        // current simple implementation.
        // For this specific method requiring generated keys, we might keep the raw JDBC
        // or enhance the base class.
        // Given the plan was to simplify, let's keep raw JDBC for 'save' if it needs
        // keys,
        // OR enhance executeUpdate to return keys.
        // For now, to stick to the plan of "simplifying", I will leave 'save' as is if
        // it's too complex to refactor
        // without changing the base class signature significantly, BUT I can refactor
        // the others.
        // Actually, let's refactor 'save' to use a new helper if possible, but the base
        // class helper I added
        // doesn't support generated keys.
        // Let's leave 'save' with raw JDBC for now as it's a special case, and refactor
        // the rest.

        try (Connection connection = getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql, new String[] { "Artist_id" })) {

            pstmt.setString(1, entity.getName());
            pstmt.setString(2, entity.getGender());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating artist failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
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
        return executeQueryOne(sql, this::mapToArtist, id);
    }

    @Override
    public List<Artist> findAll() {
        String sql = "SELECT * FROM ARTISTS";
        return executeQuery(sql, this::mapToArtist);
    }

    public long deleteById(long id) {
        String sql = "DELETE FROM ARTISTS WHERE Artist_id = ?";
        return executeUpdate(sql, id);
    }

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

        return executeCount(sql.toString(), params.toArray());
    }

    public List<Artist> searchArtists(String name, boolean nameExact, String gender, List<String> roles) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT a.Artist_id, a.Name, a.Gender FROM ARTISTS a ");

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

        sql.append(" ORDER BY a.Name ASC");

        return executeQuery(sql.toString(), this::mapToArtist, params.toArray());
    }

    private Artist mapToArtist(ResultSet rs) throws SQLException {
        return new Artist(
                rs.getLong("Artist_id"),
                rs.getString("Name"),
                rs.getString("Gender"));
    }
}