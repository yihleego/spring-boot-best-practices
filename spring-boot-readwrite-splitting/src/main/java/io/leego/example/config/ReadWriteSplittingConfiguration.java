package io.leego.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Leego Yih
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(value = "io.leego.example.repository", enableDefaultTransactions = false)
public class ReadWriteSplittingConfiguration {
}