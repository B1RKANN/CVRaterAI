package com.birkann.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@ComponentScan(basePackages = {"com.birkann"})
@EntityScan(basePackages = {"com.birkann"})
@EnableJpaRepositories(basePackages = {"com.birkann"})
@SpringBootApplication
public class CvrateraiApplicationStarter {

	public static void main(String[] args) {
		SpringApplication.run(CvrateraiApplicationStarter.class, args);
	}

}
