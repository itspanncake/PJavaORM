package fr.panncake.pjavaorm.generator;

import fr.panncake.pjavaorm.annotations.Column;

import java.lang.reflect.Field;

public class MySqlMapper extends SqlTypeMapper {
    public MySqlMapper() {
        typeMapping.put(String.class, "VARCHAR");
        typeMapping.put(int.class, "INT");
        typeMapping.put(long.class, "BIGINT");
        typeMapping.put(boolean.class, "BOOLEAN");
        typeMapping.put(double.class, "DOUBLE");
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
        return "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
    }
}
