package cwchoiit.chat.server.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 데이터베이스를 Source - Replica 이중 구성으로 변경
 * Source - 쓰기 작업에만 사용될 것
 * Replica - 읽기 작업에만 사용될 것
 * 그렇게 두 데이터베이스를 모두 사용하여 부하를 분산하기 위해선, DataSource 정보를 두 개 모두 만들어줘야 한다.
 * 이를 위한 Configuration 클래스.
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    /**
     * Source DataSource Bean
     *
     * @return Source 데이터베이스에 대한 DataSource 객체
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.source.hikari")
    public DataSource sourceDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    /**
     * Replica DataSource Bean
     *
     * @return Replica 데이터베이스에 대한 DataSource 객체
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica.hikari")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    /**
     * 데이터베이스에 대한 요청이 들어올 때, 어떤 DataSource를 사용할 지 라우팅해주는 DataSource.
     * 이 경우, 트랜잭션이 Read-Only 일 땐, Replica를 사용하게 하고 그 외엔 Source를 사용하게 한다.
     *
     * @param source  Source DataSource
     * @param replica Replica DataSource
     * @return {@link AbstractRoutingDataSource}
     * @throws SQLException Replica DataSource의 커넥션 풀을 활성화 시키지 못하는 경우
     */
    @Bean
    public DataSource routingDataSource(@Qualifier("sourceDataSource") DataSource source,
                                        @Qualifier("replicaDataSource") DataSource replica) throws SQLException {
        AbstractRoutingDataSource abstractRoutingDataSource = new AbstractRoutingDataSource() {

            @Override
            protected Object determineCurrentLookupKey() {
                String key = TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "replica" : "source";
                log.info("[determineCurrentLookupKey] Routing to {} Datasource", key);
                return key;
            }
        };

        Map<Object, Object> targetDataSources = Map.of("source", source, "replica", replica);

        abstractRoutingDataSource.setDefaultTargetDataSource(source);
        abstractRoutingDataSource.setTargetDataSources(targetDataSources);

        // 이 라인은, LazyConnectionDataSource를 사용하게 되면 커넥션 풀을 서버가 띄워질때 미리 만들어두는 게 아니라
        // 사용하게 되는 그 시점까지 커넥션 풀 생성을 미루는데, 커넥션 풀을 미리 생성해두지 않으면 처음 요청을 처리할 때 문제가 발생할 수 있어 미리 커넥션 풀만 생성
        try (Connection connection = replica.getConnection()) {
            log.info("[routingDataSource] Replica Connection Pool Warm up.");
        }

        return abstractRoutingDataSource;
    }

    /**
     * LazyConnectionDataSourceProxy를 사용하는 이유는, 트랜잭션이 Read-Only 일 땐 Replica DataSource를 사용해야 하는데,
     * 스프링 부트의 처리 순서가 커넥션을 먼저 얻고 트랜잭션을 시작한다. 그렇게 되면 어떤 문제가 발생하냐면, 위에
     * routingDataSource Bean 에서 determineCurrentLookupKey() 메서드에서 현재 트랜잭션이 Read-Only 인지 판단을 하고
     * Read-Only 인 경우에 replica를 반환하고, replica 일 때 targetDataSources 에서 해당 키로 DataSource를 가져오는데,
     * 커넥션을 먼저 얻는 경우엔 트랜잭션이 Read-Only 여도 해당 트랜잭션을 읽기 전이기 때문에 무조건 Default Target DataSource를 가져오게 된다.
     * 그래서, 이 커넥션을 최대한 늦게 (트랜잭션이 실제 시작하는 그 시점에) 얻기 위해 LazyConnectionDataSourceProxy를 사용한다.
     *
     * @param routing {@code routingDataSource}
     * @return {@link LazyConnectionDataSourceProxy}
     */
    @Bean
    @Primary
    public DataSource lazyConnectionDataSource(@Qualifier("routingDataSource") DataSource routing) {
        return new LazyConnectionDataSourceProxy(routing);
    }
}
