package io.leego.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Leego Yih
 */
@Data
@ConfigurationProperties("validation")
public class ValidationProperties {
    private String username = "^[A-Za-z0-9]{6,20}$";
}
