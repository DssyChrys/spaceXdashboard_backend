package com.example.spaceXdashboard_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SpaceXdashboardBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpaceXdashboardBackendApplication.class, args);
	}

}
