package com.wralonzo.detail_shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@EnableJpaAuditing
@SpringBootApplication
public class DetailShopApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
			return application.sources(DetailShopApplication.class);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(DetailShopApplication.class, args);
	}

}
