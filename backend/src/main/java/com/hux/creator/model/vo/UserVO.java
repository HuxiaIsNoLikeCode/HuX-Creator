package com.hux.creator.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

    private LocalDateTime createTime;
}
