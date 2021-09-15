package com.sowevo.bjjnts.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sowevo.bjjnts.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@Slf4j
public class OCRUtils {
    private static String BAIDU_ACCESS_TOKEN;
    public static final String BAIDU_OCR_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic";
    public static String TR_OCR_URL;
    public static String OCR_TYPE;


    public OCRUtils(Config config) {
        OCRUtils.BAIDU_ACCESS_TOKEN = config.getAccessToken();
        OCRUtils.TR_OCR_URL = config.getTrWebOCRUrl();
        OCRUtils.OCR_TYPE = config.getOcrType();
    }

    public static String doOcr(String src) {
        if (OCR_TYPE.equals("baidu")){
            return doBaiDuOcr(src);
        } else if(OCR_TYPE.equals("tr")){
            return doTrOcr(src);
        } else {
            log.info("未知的OCR类型!");
            throw new RuntimeException();
        }
    }

    /**
     * 使用TrWebOCR识别
     *
     * @param src src
     * @return {@link String}
     */
    public static String doTrOcr(String src) {
        src = src.replace("data:image/png;base64,","");
        // 请求url
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("img",src);
        String result = HttpUtil.post(TR_OCR_URL, paramMap);
        JSONObject jsonObject = JSONUtil.parseObj(result);
        String code = String.valueOf(jsonObject.getByPath("data.raw_out[0][1]"));
        if (code!=null){
            code = code.replaceAll("\\|","1");
            code = code.replaceAll("十","+");
            code = code.replaceAll("I","1");
            code = code.replaceAll("l","1");
            code = code.replaceAll("÷","+");
            code = code.replaceAll("·","-");
            code = code.replaceAll("\\.","-");
            code = code.replaceAll(" ","");
            code = code.replaceAll("①","0");
        }
        return code;
    }

    /**
     * 使用百度光学字符识别代码
     *
     * @param src src
     * @return {@link String}
     */
    public static String doBaiDuOcr(String src) {
        if (StrUtil.isBlank(BAIDU_ACCESS_TOKEN)){
            System.err.println("木有accessToken,不能识别验证码,再见!");
            System.exit(0);
        }
        // 请求url
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("image",src);
        paramMap.put("access_token",BAIDU_ACCESS_TOKEN);
        String result = HttpUtil.post(BAIDU_OCR_URL, paramMap);
        JSONObject jsonObject = JSONUtil.parseObj(result);
        return String.valueOf(jsonObject.getByPath("words_result[0].words"));
    }
}