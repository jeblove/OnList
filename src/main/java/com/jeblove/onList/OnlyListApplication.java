package com.jeblove.onList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

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
