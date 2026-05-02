package com.mritunjay.zomato.oms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class ZomatoOmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZomatoOmsApplication.class, args);
	}

}
