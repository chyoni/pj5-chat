package cwchoiit.chat.server;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Transactional
@Import(EmbeddedRedis.class)
public class SpringBootTestConfiguration {

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        MySQLContainer<?> mysql = MySqlTestContainer.getInstance();
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }
}
