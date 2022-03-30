package io.leego.example.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Leego Yih
 */
@Configuration
@EnableConfigurationProperties(ValidationProperties.class)
public class ValidationConfiguration {
}