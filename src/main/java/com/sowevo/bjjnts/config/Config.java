package com.sowevo.bjjnts.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author dongjunqi
 * @version 1.0
 * @className config
 * @description TODO
 * @date 2021/9/13 12:57 下午
 * @email dongjq@nancal.com
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
     * 访问令牌
     */
    private String accessToken;
}