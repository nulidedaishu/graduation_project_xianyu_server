package xyz.yaungyue.secondhand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("xyz.yaungyue.secondhand.mapper")
public class SecondhandPlatformServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondhandPlatformServerApplication.class, args);
    }

}
