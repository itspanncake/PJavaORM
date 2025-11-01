package fr.panncake.pjavaorm.core;

import java.util.HashMap;
import java.util.Map;

public class RepositoryFactory {
    private final Map<Class<?>, Repository<?>> repositoryCache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Repository<T> getRepository(Class<T> entityClass) {
        if (!repositoryCache.containsKey(entityClass)) {
            repositoryCache.put(entityClass, new GenericRepository<>(entityClass));
        }
        return (Repository<T>) repositoryCache.get(entityClass);
    }
}
