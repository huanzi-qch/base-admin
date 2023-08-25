package cn.huanzi.qch.baseadmin.config.cors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * cors安全跨域配置
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer(){
        return new WebMvcConfigurer(){
            @Override
            public void addCorsMappings(CorsRegistry registry){
                registry
                        //需要跨域的接口，例如：/openApi/*
                        .addMapping("/openApi/*")
                        //允许调用的域，例如：http://172.0.0.1:8888
                        .allowedOrigins("*")
                        //接口调用方式，POST、GET等
                        .allowedMethods("*")
                        //允许header属性
                        .allowedHeaders("*")
                        //允许cookie
                        .allowCredentials(true);
            }
        };
    }
}
