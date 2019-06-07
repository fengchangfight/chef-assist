package com.chef.assist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChefAssistApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChefAssistApplication.class, args);
	}

}
