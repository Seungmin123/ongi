package com.ongi.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableCaching
@SpringBootApplication
public class OngiApplication {

	// TODO Controller
	// TODO Service
	// TODO command
	// TODO CommonResponse


	public static void main(String[] args) {
		SpringApplication.run(OngiApplication.class, args);
	}

}
