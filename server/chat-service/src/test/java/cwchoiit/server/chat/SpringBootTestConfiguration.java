package cwchoiit.server.chat;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Transactional
@ActiveProfiles("test")
@Import(EmbeddedRedis.class)
public class SpringBootTestConfiguration {

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        MySQLContainer<?> mysql = MySqlTestContainer.getInstance();
        registry.add("TEST_DB_URL", mysql::getJdbcUrl);
        registry.add("TEST_DB_USERNAME", mysql::getUsername);
        registry.add("TEST_DB_PASSWORD", mysql::getPassword);
        registry.add("TEST_DB_DRIVER", mysql::getDriverClassName);
    }
}
