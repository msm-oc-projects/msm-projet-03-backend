package com.chatop.api.config;

import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class DotEnvConfig {
    static {
        try {
            Dotenv.configure()
                .directory(Paths.get("").toAbsolutePath().toString())
                .ignoreIfMissing()
                .load();
        } catch (Exception e) {
            System.err.println("Warning: Could not load .env file: " + e.getMessage());
        }
    }
}
