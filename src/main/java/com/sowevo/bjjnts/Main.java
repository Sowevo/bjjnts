package com.sowevo.bjjnts;

import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.sowevo.bjjnts.config.Config;
import com.sowevo.bjjnts.config.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

/**
 * @author dongjunqi
 * @version 1.0
 * @className
 * @description 启动类
 * @date 2021/9/13 12:39 下午
 * @email dongjq@nancal.com
 */
@SpringBootApplication
@Slf4j
public class Main implements CommandLineRunner {
    @Autowired
    private Config config;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args){
        getOsInfo();

        List<User> userlist = config.getUserlist();
        for (int i = 0; i < userlist.size(); i++) {
            User user = userlist.get(i);
            String username = user.getUsername();
            String password = user.getPassword();
            int index = i;
            new Thread(() -> {
                WatchVideo watchVideo = new WatchVideo(username,password,config, index);
                try {
                    watchVideo.watch();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            },username).start();
            log.info("{}:线程启动",username);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }
    }

    /**
     * 得到操作系统信息,设置浏览器驱动
     */
    private void getOsInfo() {
        OsInfo osInfo = SystemUtil.getOsInfo();
        log.info("系统信息:");
        log.info(String.valueOf(osInfo));
        String driverName = "";
        if (osInfo.isWindows()){
            driverName = "driver/chromedriver_win";
        } else if(osInfo.isLinux()){
            driverName = "driver/chromedriver_linux64";
        } else if (osInfo.isMac()){
            driverName="driver/chromedriver_mac64";
        } else {
            log.info("你这是什么系统,再见!!");
            System.exit(0);
        }
        System.setProperty("webdriver.chrome.driver", driverName);
        log.info("当前系统为{},加载{}驱动",osInfo.getName(),driverName);
    }
}