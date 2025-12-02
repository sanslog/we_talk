package com.linghang.we_talk.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "格式错误")
    private String username;
    @NotBlank(message = "格式错误")
    private String password;
}
