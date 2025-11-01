package fr.panncake.pjavaorm;

import fr.panncake.pjavaorm.core.ConnectionManager;
import fr.panncake.pjavaorm.core.Repository;
import fr.panncake.pjavaorm.core.RepositoryFactory;
import fr.panncake.pjavaorm.generator.SchemaGenerator;

import java.sql.SQLException;

public class PJavaORM {
    private static RepositoryFactory repositoryFactory;

    public static void init(String url, String user, String password, String dbType, Class<?>... entityClasses) throws SQLException {
        ConnectionManager.initialize(url, user, password);

        SchemaGenerator generator = new SchemaGenerator(dbType);

        try (java.sql.Connection conn = ConnectionManager.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {

            for (Class<?> entityClass : entityClasses) {
                String sql = generator.generateCreateTableSql(entityClass);
                if (sql != null) {
                    stmt.execute(sql);
                }
            }
        }
        repositoryFactory = new RepositoryFactory();
    }

    public static <T> Repository<T> getRepository(Class<T> entityClass) {
        if (repositoryFactory == null) {
            throw new IllegalStateException("PancakeORM not initialized. Call init() first.");
        }
        return repositoryFactory.getRepository(entityClass);
    }
}
