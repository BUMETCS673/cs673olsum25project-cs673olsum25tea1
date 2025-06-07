package com.bu.getactivecore.confg;

import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Test configuration for setting up an in-memory H2 database for testing purposes.
 */
@TestConfiguration
public class TestDatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false")
                .driverClassName("org.h2.Driver")
                .username("sa")
                .password("sa")
                .build();
    }

    @Bean
    public JpaProperties jpaProperties() {
        JpaProperties props = new JpaProperties();
        props.getProperties().put("hibernate.hbm2ddl.auto", "create-drop");
        props.getProperties().put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        return props;
    }
}