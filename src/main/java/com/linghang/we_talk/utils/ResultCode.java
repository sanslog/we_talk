package com.linghang.we_talk.utils;

import lombok.Getter;

/**
 * 状态码枚举
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源未找到"),
    BAD_REQUEST(400,"请求参数不合法");

    private final Integer code;
    private final String message;

    //构造方法默认私有
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
