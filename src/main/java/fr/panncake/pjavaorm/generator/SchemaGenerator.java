package fr.panncake.pjavaorm.generator;

import fr.panncake.pjavaorm.annotations.Column;
import fr.panncake.pjavaorm.annotations.Entity;
import fr.panncake.pjavaorm.annotations.Id;

import java.lang.reflect.Field;

public class SchemaGenerator {
    private final SqlTypeMapper mapper;
    private final String dbType;

    public SchemaGenerator(String dbType) {
        this.dbType = dbType.toLowerCase();
        switch (this.dbType) {
            case "mysql", "mariadb" -> this.mapper = new MySqlMapper();
            case "sqlite" -> this.mapper = new SQLiteMapper();
            case "postgresql", "postgres" -> this.mapper = new PostgresMapper();
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }

    public String generateCreateTableSql(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Entity.class)) return null;

        Entity entityAnno = entityClass.getAnnotation(Entity.class);
        String tableName = entityAnno.name().isEmpty() ? entityClass.getSimpleName() : entityAnno.name();

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(tableName).append(" (\n");

        String primaryKey = null;

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class)) {
                Column columnAnno = field.getAnnotation(Column.class);
                String columnName = (columnAnno != null && !columnAnno.name().isEmpty())
                        ? columnAnno.name() : field.getName();

                String sqlType = mapper.getSqlType(field);

                sql.append("  ").append(columnName).append(" ").append(sqlType);

                if (field.isAnnotationPresent(Id.class)) {
                    primaryKey = columnName;

                    if (dbType.equals("mysql") || dbType.equals("mariadb")) {
                        if (field.getType() == int.class || field.getType() == long.class) {
                            sql.append(" AUTO_INCREMENT");
                        }
                    }
                    if (dbType.equals("postgresql") || dbType.equals("postgres")) {
                        if (field.getType() == int.class) {
                            sql.replace(sql.length() - sqlType.length(), sql.length(), "SERIAL");
                        } else if (field.getType() == long.class) {
                            sql.replace(sql.length() - sqlType.length(), sql.length(), "BIGSERIAL");
                        }
                    }
                }

                if (columnAnno != null && !columnAnno.nullable()) {
                    sql.append(" NOT NULL");
                }

                sql.append(",\n");
            }
        }

        if (primaryKey != null) {
            sql.append("  PRIMARY KEY (").append(primaryKey).append(")\n");
        } else {
            sql.setLength(sql.length() - 2);
            sql.append("\n");
        }

        sql.append(") ").append(mapper.getTableCreationOptions()).append(";\n");
        return sql.toString();
    }
}
