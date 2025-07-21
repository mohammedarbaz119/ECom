package com.test.commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@RestController
public class CommerceApplication {
	public static void main(String[] args) {
		SpringApplication.run(CommerceApplication.class, args);
	}
	@RequestMapping("/**")
	public ResponseEntity<String> fallbackHandler() {
		return ResponseEntity.status(404).body("No handler found for this path");
	}
}
