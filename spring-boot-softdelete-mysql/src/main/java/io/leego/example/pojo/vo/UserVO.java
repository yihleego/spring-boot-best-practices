package io.leego.example.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Leego Yih
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String password;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
