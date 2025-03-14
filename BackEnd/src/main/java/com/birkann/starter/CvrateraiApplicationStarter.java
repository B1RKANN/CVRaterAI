package com.birkann.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = {"com.birkann"})
@EntityScan(basePackages = {"com.birkann"})
@EnableJpaRepositories(basePackages = {"com.birkann"})
@SpringBootApplication
@EnableScheduling
public class CvrateraiApplicationStarter {

	public static void main(String[] args) {
		SpringApplication.run(CvrateraiApplicationStarter.class, args);
	}

}
