package knu.database.musicbase.dao;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface BasicDataAccessObject<T, K extends Serializable> {

    public T save(T entity) throws SQLException;

    public Optional<T> findById(K id);

    public List<T> findAll();
}
