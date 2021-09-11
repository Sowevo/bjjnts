package com.sowevo.bjjnts;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dongjunqi
 * @version 1.0
 * @className WatchVideo
 * @description 看视频
 * @date 2021/9/10 10:31 上午
 * @email i@sowevo.com
 */
public class WatchVideo {
    public static final String FAKE_DEVICE = "--use-fake-device-for-media-stream";
    public static final String FAKE_UI = "--use-fake-ui-for-media-stream";
    public static final String FAKE_VIDEO = "--use-file-for-fake-video-capture=face/";
    public static final String FAKE_AUDIO = "--use-file-for-fake-audio-capture=face/";
    public static final String HOME_URL = "https://www.bjjnts.cn/home";
    public static final String LOGIN_URL = "https://www.bjjnts.cn/user/login";
    public static final String STUDY_URL = "https://www.bjjnts.cn/mine/student/study";
    public static final String VIDEO_URL = "https://www.bjjnts.cn/study/video";
    public static Map<Integer,String> cache = new HashMap<>();
    private final WebDriver driver;
    private final WebDriver.Navigation navigation;
    private final String username;
    private final String password;
    private String name = "";
    private int lessonIndex = 0;
    private boolean needSleep = false;



    public WatchVideo(String username, String password) {
        ChromeOptions options = new ChromeOptions();
        // 添加一些chrome启动时的参数
        options.addArguments("--no-sandbox");
        options.addArguments(FAKE_DEVICE);
        options.addArguments(FAKE_UI);
        options.addArguments(FAKE_VIDEO+username+".y4m");
        options.addArguments(FAKE_AUDIO+username+".wav");

        Setting setting = new Setting("users.setting");
        String headless = setting.get("system", "headless");
        //无头模式
        if (Boolean.parseBoolean(headless)){
            options.addArguments("--headless");
        }

        // 启动Chromes
        this.driver = new ChromeDriver(options);
        this.driver.manage().window().setSize(new Dimension(800,480));
        this.navigation = driver.navigate();
        this.password = password;
        this.username = username;
    }

    public void test() throws InterruptedException {
        String url = "https://webrtc.github.io/samples/src/content/peerconnection/pc1/";
        // 启动Chromes
        this.driver.get(url);
        this.driver.findElement(By.id("startButton")).click();
        Thread.sleep(3000);
        this.driver.findElement(By.id("callButton")).click();
    }

    public void watch() throws InterruptedException {
        driver.get(HOME_URL);
        while (!needSleep) {
            Thread.sleep(5000);
            String currentUrl = this.driver.getCurrentUrl();
            if (currentUrl.contains(LOGIN_URL)) {
                doLogin();
            } else if (currentUrl.contains(HOME_URL)) {
                doHome();
            } else if (currentUrl.contains(STUDY_URL)) {
                doStduy();
            } else if (currentUrl.contains(VIDEO_URL)) {
                doVideo();
            } else if (currentUrl.contains("https://www.bjjnts.cn/study/exam")) {
                System.err.println(name+":跳过单元测式");
                this.navigation.to(HOME_URL);
            } else if (currentUrl.contains("https://www.bjjnts.cn/study/courseware")) {
                System.err.println(name+":跳过单元考试");
                this.navigation.to(HOME_URL);
            }
        }
    }


    /**
     * base64转图像
     *
     * @param imgStr  img str
     * @param imgFile img文件
     * @return boolean
     */
    public boolean base64ToImage(String imgStr, File imgFile) { // 对字节数组字符串进行Base64解码并生成图片
        if (StrUtil.isEmpty(imgStr)) {
            return false;
        }
        imgStr = imgStr.replace("data:image/png;base64,","");
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            // Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            OutputStream out = new FileOutputStream(imgFile);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getTime(){
        List<WebElement> current = driver.findElements(By.cssSelector("span[class='current-time']"));
        List<WebElement> duration = driver.findElements(By.cssSelector("span[class='duration']"));
        if (!current.isEmpty()&&!duration.isEmpty()){
            String currentTime = current.get(0).getAttribute("textContent");
            String durationTime = duration.get(0).getAttribute("textContent");
            if (StrUtil.isNotBlank(currentTime)&&StrUtil.isNotBlank(durationTime)){
                return currentTime+"/"+durationTime;
            }
        }
        return "";
    }

    /**
     * 元素是否存在
     *
     * @param selector 选择器
     * @return boolean
     */
    public boolean checkElementExits(By selector){
        try {
            driver.findElement(selector);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /**
     * 登录页面处理
     *
     */
    public void doLogin(){
        try {
            //获取输入框的id,并在输入框中输入用户名
            WebElement loginInput = driver.findElement(By.id("login-hooks_username"));
            loginInput.sendKeys(username);

            //获取输入框的id，并在输入框中输入密码
            WebElement pwdInput = driver.findElement(By.id("login-hooks_password"));
            pwdInput.sendKeys(password);

            //获取登陆按钮的className，并点击
            WebElement loginBtn = driver.findElement(By.xpath("//button[@type='submit']"));
            loginBtn.click();
            System.err.println(username+":登录!");
        } catch (Exception ignored) {
        }
    }

    /**
     * 首页处理
     *
     */
    public void doHome(){
        try {
            boolean login = !checkElementExits(By.xpath("//div[starts-with(@class,'login_register')]"));
            if (!login){
                navigation.to(LOGIN_URL);
            } else {
                navigation.to(STUDY_URL);
            }
        } catch (Exception ignored) {

        }
    }

    /**
     * 学习页面处理
     *
     */
    public void doStduy(){
        try {
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.startsWith(STUDY_URL)){
                //加载到指定url
                navigation.to(STUDY_URL);
            }

            WebElement nameStr = driver.findElement(By.cssSelector("div[class^='mobile___']"));
            this.name = StrUtil.fillAfter(nameStr.getAttribute("textContent"), '　',3);

            List<WebElement> elements = driver.findElements(By.cssSelector("a[class^='lesson_list_item___']"));
            if (elements.isEmpty()){
                System.err.println(name+":你好像没有要学习的课程");
                navigation.refresh();
            } else {
                if (lessonIndex > 1){
                    driver.findElement(By.cssSelector("div[class^='open_list___']")).click();
                }
                if (elements.size()>lessonIndex){
                    System.err.println(name+":开始学习\t"+elements.get(lessonIndex).getText());
                    elements.get(lessonIndex).click();
                } else {
                    lessonIndex = 0;
                    elements.get(lessonIndex).click();
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 检查停止按钮是否存在
     *
     * @return boolean
     */
    public boolean checkStopButton(){
        try {
            driver.findElement(By.cssSelector("[class='prism-play-btn playing']"));
            String time = getTime();
            String title = driver.getTitle().replace("-北京市职业技能提升行动管理平台","");
            System.err.println(name+":正在播放\t"+StrUtil.fillAfter(title, '　',20)+StrUtil.fillAfter(time, ' ',18));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查播放按钮是否存在
     * 有播放按钮就点一下
     *
     * @return boolean
     */
    public boolean checkPlayButton(){
        try {
            WebElement element = driver.findElement(By.cssSelector("[class='prism-big-play-btn loading-center']"));
            if (element.isDisplayed()){
                element.click();
            }
            System.err.println(name+":点了一下播放按钮~");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void checkError(){
        //视频解析失败了，请几分钟后再试。您可以尝试切换清晰度或先学习其他章节。
        //[4400]由于服务器或网络原因不能加载资源，或者格式不支持
        try {
            driver.findElement(By.cssSelector("div[class='video_error_wrap']"));
            navigation.refresh();
        } catch (Exception ignored) {

        }
    }

    /**
     * 检查人脸识别
     * 看到确认按钮就点一下....
     */
    public void checkFace() {
        try {
            WebElement codeBtn = driver.findElement(By.cssSelector("button[class='ant-btn ant-btn-primary']"));
            codeBtn.click();
        } catch (Exception ignored) {}
    }

    /**
     * 检查需要休息
     */
    public void checkNeedSleep() {
        try {
            WebElement timeOutText = driver.findElement(By.cssSelector("span[class='ant-modal-confirm-title']"));
            if (timeOutText.getAttribute("textContent").contains("快去休息吧")){
                needSleep = true;
                System.err.println(name+":"+timeOutText.getAttribute("textContent"));
            }
        } catch (Exception ignored) {}
    }

    public void checkCode() {
        try {
            WebElement codeImage = driver.findElement(By.cssSelector("div[class^='code_box___']>img"));
            String src = codeImage.getAttribute("src");
            File file = FileUtil.file(".","image", System.currentTimeMillis() + ".png");
            base64ToImage(src,file);

            String code;
            if (cache.containsKey(src.hashCode())){
                code = cache.get(src.hashCode());
            } else {
                if (cache.size()>100){
                    cache.clear();
                }
                code = BaiDuOcr.doOcr(src);
                System.err.println(name+":识别结果为:"+code);
                cache.put(src.hashCode(),code);
            }

            //code处理
            if (!StrUtil.isNumeric(code)){
                if (code.contains("+")||code.contains("-")||code.contains("x")||code.contains("X")||code.contains("/")||code.contains("*")){
                    code = String.valueOf(FormulaCalculator.getResult(code));
                } else {
                    code = "1234";
                }
            }

            WebElement codeInput = driver.findElement(By.xpath("//*[@id='basic_code']"));
            codeInput.sendKeys(code);
            codeInput.sendKeys(Keys.HOME,Keys.chord(Keys.SHIFT,Keys.END),code);
            WebElement codeBtn = driver.findElement(By.cssSelector("button[class='ant-btn ant-btn-primary']"));
            codeBtn.click();

        } catch (Exception ignored) {}
    }

    /**
     * 老板换碟!
     *
     */
    public void switchVideo(){
        try {
            List<WebElement> elements = driver.findElements(By.cssSelector("a[href^='/study/video']"));
            System.err.println(name+":视频总数:"+elements.size());
            List<WebElement> list = elements.stream().filter(e -> e.findElements(By.cssSelector("span[role='img']")).isEmpty()).collect(Collectors.toList());
            System.err.println(name+":未学习视频总数:"+list.size());
            if (list.isEmpty()){
                lessonIndex ++;
                navigation.to(HOME_URL);
            } else {
                String href = list.get(0).getAttribute("href");
                navigation.to(href);
            }
        } catch (Exception ignored) {}
    }

    public boolean checkFinish(){
        //判断是否有此图片（播放条拖拽图片）有则已完成
        boolean playTagExits = checkElementExits(By.cssSelector("#J_prismPlayer"));
        if (playTagExits){
            boolean elementExits = checkElementExits(By.cssSelector("img[src$='https://g.alicdn.com/de/prismplayer/2.9.3/skins/default/img/dragcursor.png']"));
            if (elementExits){
                return true;
            }
        }
        //寻找下一集按钮
        boolean nextBtnExits = checkElementExits(By.cssSelector("button[class^='next_button___']"));
        if (nextBtnExits){
            return true;
        }
        //寻找重播按钮
        boolean repBtnExits = checkElementExits(By.cssSelector("button[class^='reset_button___']"));
        if (repBtnExits){
            return true;
        }
        return false;
    }

    /**
     * 播放视频页面处理
     *
     *
     */
    public void doVideo(){
        //处理验证码
        checkCode();
        //处理人脸识别
        checkFace();
        //判断当前视频是否播放完成
        boolean finish = checkFinish();
        if (finish){
            //看完了换碟
            switchVideo();
        } else {
            //没看完检查下播放状态.处理视频状态
            boolean playButton = checkPlayButton();
            boolean stopButton = checkStopButton();
            if (!playButton&&!stopButton){
                System.err.println(name+":没有播放&暂停按钮!");
            }
        }
        // 检查错误
        checkError();
        // 检查超时8小时需要休息
        checkNeedSleep();
    }
}