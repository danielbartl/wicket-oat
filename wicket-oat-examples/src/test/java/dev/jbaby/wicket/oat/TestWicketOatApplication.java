package dev.jbaby.wicket.oat;

import org.springframework.boot.SpringApplication;

public class TestWicketOatApplication {

	static void main(String[] args) {
		SpringApplication.from(WicketOatApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
