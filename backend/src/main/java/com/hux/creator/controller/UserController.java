package com.hux.creator.controller;

import com.hux.creator.common.Result;
import com.hux.creator.model.dto.LoginDTO;
import com.hux.creator.model.dto.RegisterDTO;
import com.hux.creator.model.vo.UserVO;
import com.hux.creator.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Validated @RequestBody RegisterDTO registerDTO) {
        return Result.success(userService.register(registerDTO));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserInfo(userId));
    }
}
