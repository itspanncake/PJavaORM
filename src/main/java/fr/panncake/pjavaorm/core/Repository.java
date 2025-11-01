package fr.panncake.pjavaorm.core;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Repository<T> {
    T save(T entity) throws SQLException;
    Optional<T> findById(Object id) throws SQLException;
    List<T> findAll() throws SQLException;
    void delete(Object id) throws SQLException;
}
