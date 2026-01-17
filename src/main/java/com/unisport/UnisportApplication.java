package com.unisport;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * UniSport 应用启动类
 *
 * @author UniSport Team
 */
@SpringBootApplication
@MapperScan("com.unisport.mapper")
@EnableScheduling  // 开启定时任务
public class UnisportApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnisportApplication.class, args);
        System.out.println("""
                
                ===================================
                UniSport Backend Started Successfully!
                API Documentation: http://localhost:8080/doc.html
                ===================================
                """);
    }
}
