package com.aaronjosh.real_estate_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RealEstateAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealEstateAppApplication.class, args);
	}

}
