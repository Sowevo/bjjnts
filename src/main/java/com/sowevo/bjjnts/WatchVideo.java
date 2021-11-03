package com.sowevo.bjjnts;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.sowevo.bjjnts.config.Config;
import com.sowevo.bjjnts.utils.FormulaCalculator;
import com.sowevo.bjjnts.utils.OCRUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 * @version 1.0
 * @className WatchVideo
 * @description 看视频
 * @date 2021/9/10 10:31 上午
 * @email i@sowevo.com
 */
@Slf4j
public class WatchVideo {
    private Config config;

    private static final String FULL_PATH = System.getProperty("user.dir");
    private static final String SEPARATOR = File.separator;

    public static final String FAKE_DEVICE = "--use-fake-device-for-media-stream";
    public static final String FAKE_UI = "--use-fake-ui-for-media-stream";
    public static final String FAKE_VIDEO = "--use-file-for-fake-video-capture="+FULL_PATH+SEPARATOR+"face"+SEPARATOR;
    public static final String FAKE_AUDIO = "--use-file-for-fake-audio-capture="+FULL_PATH+SEPARATOR+"face"+SEPARATOR;

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

    /**目前学习的课程序号*/
    private int lessonIndex = 0;
    /**课程循环次数,避免所有课程都学完之后无限循环*/
    private int lessonLoop = 1;
    /**是不是需要休息*/
    private boolean needSleep = false;
    /**休息结束时间*/
    private DateTime sleepTime;



    public WatchVideo(String username, String password,Config config,int index) {
        ChromeOptions options = new ChromeOptions();
        // 添加一些chrome启动时的参数
        options.addArguments("--no-sandbox");
        options.addArguments(FAKE_DEVICE);
        options.addArguments(FAKE_UI);
        options.addArguments(FAKE_VIDEO+username+".y4m");
        options.addArguments(FAKE_AUDIO+username+".wav");

        boolean headless = config.isHeadless();
        //无头模式
        if (headless){
            options.addArguments("--headless");
        }

        boolean muteAudio = config.isMuteAudio();
        // 静音
        if (muteAudio){
          options.addArguments("--mute-audio");
        }
        
        // 启动Chromes
        this.driver = new ChromeDriver(options);
        if (!headless){
            this.driver.manage().window().setSize(new Dimension(800,480));
            this.driver.manage().window().setPosition(new Point(index*100,index*100));
        }
        this.navigation = driver.navigate();
        this.password = password;
        this.username = username;
        this.config = config;
    }

    public void test() throws InterruptedException {
        String url = "https://webrtc.github.io/samples/src/content/peerconnection/pc1/";
        // 启动Chromes
        this.driver.get(url);
        this.driver.findElement(By.id("startButton")).click();
        setTitle(username);
        Thread.sleep(6000);
        this.driver.findElement(By.id("callButton")).click();
    }

    public void watch() throws InterruptedException {
        driver.get(HOME_URL);
        while (true) {
            if (needSleep&&sleepTime!=null){
                if (DateUtil.compare(new Date(),sleepTime.toJdkDate())<=0){
                    log.info("{}:今天已经学够8小时了",name);
                    setTitle(":今天已经学够8小时了");
                    Thread.sleep(1000*60*10);
                } else {
                    sleepTime = null;
                    needSleep = false;
                }
            } else if (lessonLoop>3){
                log.info("{}:所有课程都学完了",name);
                setTitle(":所有课程都学完了");
                break;
            } else {
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
                    log.info("{}:跳过单元测式",name);
                    this.navigation.to(HOME_URL);
                } else if (currentUrl.contains("https://www.bjjnts.cn/study/courseware")) {
                    log.info("{}:跳过单元考试",name);
                    this.navigation.to(HOME_URL);
                }
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
    public void base64ToImage(String imgStr, File imgFile) { // 对字节数组字符串进行Base64解码并生成图片
        imgStr = imgStr.replace("data:image/png;base64,","");
        if (StrUtil.isEmpty(imgStr)) {
            return;
        }
        BufferedImage image = ImgUtil.toImage(imgStr);
        ImgUtil.write(image,imgFile);
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
            loginInput.sendKeys(Keys.HOME,Keys.chord(Keys.SHIFT,Keys.END),username);

            //获取输入框的id，并在输入框中输入密码
            WebElement pwdInput = driver.findElement(By.id("login-hooks_password"));
            pwdInput.sendKeys(Keys.HOME,Keys.chord(Keys.SHIFT,Keys.END),password);

            //获取登陆按钮的className，并点击
            WebElement loginBtn = driver.findElement(By.xpath("//button[@type='submit']"));
            loginBtn.click();
            log.info("{}:登录!",username);
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

            List<WebElement> elements = driver.findElements(By.cssSelector("a[class^='lesson_list_item___']>h2"));
            if (elements.isEmpty()){
                log.info("{}:你好像没有要学习的课程",name);
                navigation.refresh();
            } else {
                if (lessonIndex > 1){
                    driver.findElement(By.cssSelector("div[class^='open_list___']")).click();
                }
                if (elements.size()>lessonIndex){
                    log.info("{}:开始学习\t{}",name,elements.get(lessonIndex).getText());
                    elements.get(lessonIndex).click();
                } else {
                    log.info("{}:课程索引越界,尝试重新开始,当前循环次数{}",name,lessonLoop);
                    lessonIndex = 0;
                    lessonLoop ++;
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
            WebElement title = driver.findElement(By.cssSelector("div[class^='header_box_title___']"));
            log.info("{}:正在播放\t{}",name,StrUtil.fillAfter(title.getAttribute("textContent"), '　',20)+StrUtil.fillAfter(time, ' ',18));
            setTitle(":"+StrUtil.fillAfter(time, ' ',18));
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
            log.info("{}:点了一下播放按钮~",name);
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
            log.info("人脸识别");
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
                sleepTime = DateUtil.beginOfDay(DateUtil.tomorrow());
                setTitle(":今天已经学够8小时了");
                log.info("{}:{}",name,timeOutText.getAttribute("textContent"));
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
                code = OCRUtils.doOcr(src);
                log.info("{}:验证码识别结果为:{}",name,code);
                cache.put(src.hashCode(),code);
            }

            //code处理
            if (!StrUtil.isNumeric(code)){
                if (code.contains("+")||code.contains("-")||code.contains("x")||code.contains("X")||code.contains("*")){
                    code = String.valueOf(FormulaCalculator.getResult(code));
                } else {
                    code = String.valueOf(RandomUtil.randomInt(0,99999));
                }
            }

            WebElement codeInput = driver.findElement(By.xpath("//*[@id='basic_code']"));
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
            log.info("{}:视频总数:{}",name,elements.size());
            List<WebElement> list = elements.stream().filter(e -> e.findElements(By.cssSelector("span[role='img']")).isEmpty()).collect(Collectors.toList());
            log.info("{}:未学习视频总数:{}",name,list.size());
            if (list.isEmpty()){
                lessonIndex ++;
                log.info("{}:当前章节已经学习完毕,换下一章!",name);
                navigation.to(STUDY_URL);
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
                log.info("{}:没有播放&暂停按钮!",name);
            }
        }
        // 检查错误
        checkError();
        // 检查超时8小时需要休息
        checkNeedSleep();
    }

    private void setTitle(String title){
        title = name + title;
        JavascriptExecutor js = (JavascriptExecutor)driver;
        js.executeScript("document.title = '"+title+"'");
    }
}