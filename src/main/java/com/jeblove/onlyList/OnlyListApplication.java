package com.jeblove.onlyList;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author jeblove
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
//@EnableTransactionManagement
//@EnableScheduling
//@MapperScan(basePackages = "com.jeblove.onlyList.mapper")
public class OnlyListApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlyListApplication.class, args);
    }

}
