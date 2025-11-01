package fr.panncake.pjavaorm.generator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

abstract class SqlTypeMapper {
    protected final Map<Class<?>, String> typeMapping = new HashMap<>();
    public abstract String getSqlType(Field field);
    public abstract String getTableCreationOptions();
}
