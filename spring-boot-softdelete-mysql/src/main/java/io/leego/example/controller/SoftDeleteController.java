package io.leego.example.controller;

import io.leego.example.entity.User;
import io.leego.example.pojo.dto.UserCreateDTO;
import io.leego.example.pojo.dto.UserUpdateDTO;
import io.leego.example.pojo.vo.UserVO;
import io.leego.example.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Leego Yih
 */
@RestController
public class SoftDeleteController {
    private final UserRepository userRepository;

    public SoftDeleteController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("users/{id}")
    public UserVO getUser(@PathVariable Long id) {
        return userRepository.findById(id).map(this::toVO).orElse(null);
    }

    @PostMapping("users")
    public UserVO createUser(@Validated @RequestBody UserCreateDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("用户已存在");
        }
        User user = new User(dto.getUsername(), dto.getPassword());
        userRepository.save(user);
        return toVO(user);
    }

    @PutMapping("users")
    public UserVO updateUser(@Validated @RequestBody UserUpdateDTO dto) {
        User user = userRepository.findById(dto.getId()).orElse(null);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(dto.getPassword());
        userRepository.save(user);
        return toVO(user);
    }

    @DeleteMapping("users/{id}")
    public int deleteUser(@PathVariable Long id) {
        return userRepository.softdeleteById(id);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
