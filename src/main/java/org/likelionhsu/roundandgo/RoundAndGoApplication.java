package org.likelionhsu.roundandgo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RoundAndGoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoundAndGoApplication.class, args);
	}

}
