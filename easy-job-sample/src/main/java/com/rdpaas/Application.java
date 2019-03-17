package com.rdpaas;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;


/**
 * springboot启动类
 * @author rongdi
 * @date 2019-03-17 16:04
 */
@EnableScheduling
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean(name = "easyjobJdbcTemplate")
    public JdbcTemplate taskJdbcTemplate(
            @Qualifier("easyjobDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
