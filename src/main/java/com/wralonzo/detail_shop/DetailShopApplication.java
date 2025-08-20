package com.wralonzo.detail_shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DetailShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(DetailShopApplication.class, args);
	}

}
