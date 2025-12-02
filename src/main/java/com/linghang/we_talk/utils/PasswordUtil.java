package com.linghang.we_talk.utils;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 增强版密码工具类
 * 包含密码强度验证和多种加密方式支持
 */
@Component
public class PasswordUtil {

    private final BCryptPasswordEncoder bcryptEncoder;

    // 密码强度正则表达式
    private static final Pattern STRONG_PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    public PasswordUtil() {
        this.bcryptEncoder = new BCryptPasswordEncoder();
    }

    /**
     * BCrypt 加密密码
     */
    public String bcryptEncode(String rawPassword) {
//        validatePasswordStrength(rawPassword);
        return bcryptEncoder.encode(rawPassword);
    }

    /**
     * BCrypt 验证密码
     */
    public boolean bcryptMatches(String rawPassword, String encodedPassword) {
        return bcryptEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 验证密码强度
     * 要求：至少8位，包含大小写字母、数字、特殊字符
     */
    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("密码长度至少8位");
        }

        if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("密码必须包含大小写字母、数字和特殊字符");
        }
    }

    /**
     * 验证密码强度（宽松版）
     * 要求：至少6位
     */
    public boolean isPasswordValid(String password) {
        if (password == null) return false;
        return password.length() >= 6;
    }

    /**
     * 生成随机密码
     */
    public String generateRandomPassword(int length) {
        if (length < 8) {
            length = 8;
        }

        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()-_=+";

        String allChars = upperCase + lowerCase + numbers + specialChars;
        StringBuilder password = new StringBuilder();

        // 确保包含每种类型的字符
        password.append(upperCase.charAt((int) (Math.random() * upperCase.length())));
        password.append(lowerCase.charAt((int) (Math.random() * lowerCase.length())));
        password.append(numbers.charAt((int) (Math.random() * numbers.length())));
        password.append(specialChars.charAt((int) (Math.random() * specialChars.length())));

        // 填充剩余长度
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt((int) (Math.random() * allChars.length())));
        }

        // 打乱顺序
        return shuffleString(password.toString());
    }

    /**
     * 打乱字符串顺序
     */
    private String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = (int) (Math.random() * characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }

    /**
     * 检查是否需要升级加密（当使用较弱加密方式时）
     */
    public boolean needsUpgrade(String encodedPassword) {
        return bcryptEncoder.upgradeEncoding(encodedPassword);
    }
}
