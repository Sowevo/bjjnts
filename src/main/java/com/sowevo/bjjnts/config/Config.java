package com.sowevo.bjjnts.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * @version 1.0
 * @className config
 * @description 配置类
 * @date 2021/9/13 12:57 下午
 */
@Data
@Component
@ConfigurationProperties(prefix = "config")
public class Config {
    /**
     * 用户列表
     */
    private List<User> userlist;

    /**
     * 无头模式
     */
    private boolean headless;

    /**
     * 静音
     */
    private boolean muteAudio;
    
    /**
     * 百度的访问令牌
     */
    private String accessToken;
    /**
     * TrWebOCR的访问Url
     **/
    private String trWebOCRUrl;

    private String ocrType;
}