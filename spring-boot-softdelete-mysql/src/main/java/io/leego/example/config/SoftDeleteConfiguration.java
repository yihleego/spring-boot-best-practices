package io.leego.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author Leego Yih
 */
@Configuration
@EnableJpaAuditing
public class SoftDeleteConfiguration {
}