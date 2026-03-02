package com.teamflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TeamFlow Application entry point.
 *
 * @SpringBootApplication is a meta-annotation that combines:
 * - @Configuration: marks this class as a source of bean definitions
 * - @EnableAutoConfiguration: tells Spring Boot to auto-configure based on classpath
 * - @ComponentScan: scans all packages under com.teamflow for components
 */
@SpringBootApplication
public class TeamFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamFlowApplication.class, args);
    }
}
