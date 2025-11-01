package fr.panncake.pjavaorm.generator;

import fr.panncake.pjavaorm.annotations.Column;

import java.lang.reflect.Field;

public class PostgresMapper extends SqlTypeMapper {
    public PostgresMapper() {
        typeMapping.put(String.class, "VARCHAR");
        typeMapping.put(int.class, "INTEGER");
        typeMapping.put(long.class, "BIGINT");
        typeMapping.put(boolean.class, "BOOLEAN");
        typeMapping.put(double.class, "DOUBLE PRECISION");
    }

    @Override
    public String getSqlType(Field field) {
        Column column = field.getAnnotation(Column.class);
        String baseType = typeMapping.getOrDefault(field.getType(), "VARCHAR");

        if (baseType.equals("VARCHAR") && column != null) {
            return "VARCHAR(" + column.length() + ")";
        }
        return baseType;
    }

    @Override
    public String getTableCreationOptions() {
        return "";
    }
}
