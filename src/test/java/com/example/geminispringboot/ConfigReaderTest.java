package com.example.geminispringboot;

import com.example.geminispringboot.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConfigReaderTest implements CommandLineRunner {

    @Autowired
    private AppProperties appProperties;

    public static void main(String[] args) {
        SpringApplication.run(ConfigReaderTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Allowed Names: " + appProperties.getAllowedNames());
        System.out.println("Mappings: " + appProperties.getMappings());
        System.out.println("Mapping Keys: " + appProperties.getMappings().keySet());
        System.out.println("Test Property: " + appProperties.getTestProperty());
    }
}
