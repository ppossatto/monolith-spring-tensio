package com.ppossatto.tensio;

import org.springframework.boot.SpringApplication;

public class TestMlSbTensioApplication {

	public static void main(String[] args) {
		SpringApplication.from(Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
