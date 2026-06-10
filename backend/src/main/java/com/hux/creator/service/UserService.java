package com.hux.creator.service;

import com.hux.creator.model.dto.LoginDTO;
import com.hux.creator.model.dto.RegisterDTO;
import com.hux.creator.model.vo.UserVO;

import java.util.Map;

public interface UserService {

    Map<String, Object> register(RegisterDTO registerDTO);

    Map<String, Object> login(LoginDTO loginDTO);

    UserVO getUserInfo(Long userId);
}
