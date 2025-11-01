package fr.panncake.pjavaorm.generator;

import java.lang.reflect.Field;

public class SQLiteMapper extends SqlTypeMapper {
    public SQLiteMapper() {
        typeMapping.put(String.class, "TEXT");
        typeMapping.put(int.class, "INTEGER");
        typeMapping.put(long.class, "INTEGER");
        typeMapping.put(boolean.class, "INTEGER");
        typeMapping.put(double.class, "REAL");
    }

    @Override
    public String getSqlType(Field field) {
        return typeMapping.getOrDefault(field.getType(), "TEXT");
    }

    @Override
    public String getTableCreationOptions() {
        return "";
    }
}
