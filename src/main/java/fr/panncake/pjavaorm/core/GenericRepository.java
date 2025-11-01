package fr.panncake.pjavaorm.core;

import fr.panncake.pjavaorm.annotations.*;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenericRepository<T> implements Repository<T> {
    private final Class<T> entityClass;
    private final String tableName;
    private final Field idField;
    private final String idColumnName;

    public GenericRepository(Class<T> entityClass) {
        this.entityClass = entityClass;

        Entity entityAnno = entityClass.getAnnotation(Entity.class);
        this.tableName = entityAnno.name().isEmpty() ? entityClass.getSimpleName() : entityAnno.name();

        Field tempIdField = null;
        String tempIdColumnName = null;
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                tempIdField = field;
                field.setAccessible(true);

                Column columnAnno = field.getAnnotation(Column.class);
                tempIdColumnName = (columnAnno != null && !columnAnno.name().isEmpty())
                        ? columnAnno.name() : field.getName();
                break;
            }
        }
        if (tempIdField == null) throw new IllegalArgumentException("Entity " + entityClass.getSimpleName() + " must have a field annotated @Id.");
        this.idField = tempIdField;
        this.idColumnName = tempIdColumnName;
    }

    @Override
    public T save(T entity) throws SQLException {
        // Simplified Upsert (Insert or Update if exists) logic is complex. Here, we only INSERT.
        StringBuilder columns = new StringBuilder();
        StringBuilder valuesPlaceholders = new StringBuilder();
        List<Object> values = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);

                    Column columnAnno = field.getAnnotation(Column.class);
                    String columnName = (columnAnno != null && !columnAnno.name().isEmpty())
                            ? columnAnno.name() : field.getName();

                    columns.append(columnName).append(",");
                    valuesPlaceholders.append("?,");
                    values.add(value);

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Reflection error during save: " + e.getMessage());
                }
            }
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                columns.substring(0, columns.length() - 1),
                valuesPlaceholders.substring(0, valuesPlaceholders.length() - 1));

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.executeUpdate();

            // Handle auto-generated keys (crucial for INSERT)
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Assuming the primary key is of type Integer or Long for simplicity
                    if (idField.getType() == int.class || idField.getType() == Integer.class) {
                        idField.set(entity, generatedKeys.getInt(1));
                    } else if (idField.getType() == long.class || idField.getType() == Long.class) {
                        idField.set(entity, generatedKeys.getLong(1));
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error setting generated ID: " + e.getMessage());
            }

            return entity;
        }
    }

    @Override
    public Optional<T> findById(Object id) throws SQLException {
        String sql = String.format("SELECT * FROM %s WHERE %s = ?", tableName, idColumnName);

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEntity(rs));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<T> findAll() throws SQLException {
        List<T> entities = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s", tableName);

        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
        }
        return entities;
    }

    @Override
    public void delete(Object id) throws SQLException {
        String sql = String.format("DELETE FROM %s WHERE %s = ?", tableName, idColumnName);

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }

    private T mapResultSetToEntity(ResultSet rs) {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();

            for (Field field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);

                    Column columnAnno = field.getAnnotation(Column.class);
                    String columnName = (columnAnno != null && !columnAnno.name().isEmpty())
                            ? columnAnno.name() : field.getName();

                    Object value = rs.getObject(columnName);

                    if (value != null) {
                        if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                            if (value instanceof Number) {
                                value = ((Number) value).intValue() != 0;
                            }
                        }
                        field.set(entity, value);
                    }
                }
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping ResultSet: " + e.getMessage(), e);
        }
    }
}
