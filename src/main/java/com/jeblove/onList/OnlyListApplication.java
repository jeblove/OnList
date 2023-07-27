package com.jeblove.onList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * @author jeblove
 */
//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication()
//@ComponentScan("com.jeblove.onList.common")
//@EnableTransactionManagement
//@EnableScheduling
//@MapperScan(basePackages = "com.jeblove.onlyList.mapper")
public class OnlyListApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlyListApplication.class, args);
    }

}
