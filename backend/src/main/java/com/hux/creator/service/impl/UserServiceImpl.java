package com.hux.creator.service.impl;

import com.hux.creator.model.dto.LoginDTO;
import com.hux.creator.model.dto.RegisterDTO;
import com.hux.creator.model.entity.User;
import com.hux.creator.model.vo.UserVO;
import com.hux.creator.repository.UserRepository;
import com.hux.creator.service.UserService;
import com.hux.creator.util.JwtUtil;
import com.hux.creator.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;

    @Override
    public Map<String, Object> register(RegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordUtil.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        UserVO userVO = convertToVO(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userVO);
        return result;
    }

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));

        if (!passwordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        UserVO userVO = convertToVO(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userVO);
        return result;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return convertToVO(user);
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
