package com.oraclejava.demoboard.health;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	private final String applicationName;

	public HealthController(@Value("${spring.application.name:demo-board}") String applicationName) {
		this.applicationName = applicationName;
	}

	@GetMapping("/health")
	public Map<String, Object> health() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", "UP");
		body.put("application", applicationName);
		body.put("timestamp", Instant.now().toString());
		return body;
	}

}
