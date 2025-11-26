package com.example.aicourse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class AICourseApplication{
    public static void main(String[] args){

        SpringApplication.run(AICourseApplication.class,args);

    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}