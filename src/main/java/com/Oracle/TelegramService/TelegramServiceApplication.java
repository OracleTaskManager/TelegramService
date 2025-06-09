package com.Oracle.TelegramService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.converter.json.GsonBuilderUtils;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.Oracle")
public class TelegramServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramServiceApplication.class, args);
	}

}