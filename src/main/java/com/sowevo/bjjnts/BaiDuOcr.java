package com.sowevo.bjjnts;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dongjunqi
 * @version 1.0
 * @className BaiDuOcr
 * @description ocr
 * @date 2021/9/11 5:12 上午
 * @email i@sowevo.com
 */
public class BaiDuOcr {
    private static String accessToken;
    public static final String OCR_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic";
    static {
        Setting setting = new Setting("users.setting");
        accessToken = setting.get("system", "accessToken");
    }
    /**
     * 使用百度光学字符识别代码
     *
     * @param src src
     * @return {@link String}
     */
    public static String doOcr(String src) {
        if (StrUtil.isBlank(accessToken)){
            System.err.println("木有accessToken,不能识别验证码,再见!");
            System.exit(0);
        }
        // 请求url
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("image",src);
        paramMap.put("access_token",accessToken);
        String result = HttpUtil.post(OCR_URL, paramMap);
        JSONObject jsonObject = JSONUtil.parseObj(result);
        return String.valueOf(jsonObject.getByPath("words_result[0].words"));
    }
}