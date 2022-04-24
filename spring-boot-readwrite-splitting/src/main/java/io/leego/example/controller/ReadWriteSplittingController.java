package io.leego.example.controller;

import io.leego.example.entity.User;
import io.leego.example.pojo.dto.UserCreateDTO;
import io.leego.example.pojo.dto.UserUpdateDTO;
import io.leego.example.pojo.vo.UserVO;
import io.leego.example.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
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
public class ReadWriteSplittingController {
    private final UserRepository userRepository;

    public ReadWriteSplittingController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("users/{id}")
    public UserVO getUser(@PathVariable Long id) {
        return userRepository.findById(id).map(this::toVO).orElse(null);
    }

    @Transactional
    @PostMapping("users")
    public UserVO createUser(@Validated @RequestBody UserCreateDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("用户已存在");
        }
        User user = new User(dto.getUsername(), dto.getPassword());
        userRepository.save(user);
        return toVO(user);
    }

    @Transactional
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

    @Transactional
    @DeleteMapping("users/{id}")
    public int deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return 1;
    }

    private UserVO toVO(User user) {
        return new UserVO(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getCreatedTime(),
                user.getUpdatedTime());
    }
}
