package com.jeblove.onList.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author : Jeb
 * @date :2023/6/3 22:18
 * @classname :  CorsConfig
 * @description : 设置跨域
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*"); // 允许任何来源的跨域访问
        config.addAllowedHeader("*"); // 允许任何头部信息的跨域访问
        config.addAllowedMethod("*"); // 允许任何请求方法的跨域访问
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
