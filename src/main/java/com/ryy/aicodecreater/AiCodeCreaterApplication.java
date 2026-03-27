package com.ryy.aicodecreater;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ryy.aicodecreater.mapper")
public class AiCodeCreaterApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiCodeCreaterApplication.class, args);
	}

}
