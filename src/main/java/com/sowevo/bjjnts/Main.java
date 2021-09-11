package com.sowevo.bjjnts;

import cn.hutool.setting.Setting;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;

/**
 * @author dongjunqi
 * @version 1.0
 * @className Main
 * @description 主程序
 * @date 2021/9/9 10:43 上午
 * @email i@sowevo.com
 */
public class Main {

    public static void main(String[] args){
        OsInfo osInfo = SystemUtil.getOsInfo();
        System.err.println("系统信息:");
        System.err.println(osInfo);
        if (osInfo.isWindows()){
            System.setProperty("webdriver.chrome.driver", "driver/chromedriver_win");
        } else if(osInfo.isLinux()){
            System.setProperty("webdriver.chrome.driver", "driver/chromedriver_linux64");
        } else if (osInfo.isMac()){
            System.setProperty("webdriver.chrome.driver", "driver/chromedriver_mac64");
        } else {
            System.err.println("你这是什么系统,再见!!");
            System.exit(0);
        }

        Setting setting = new Setting("users.setting");
        setting.getGroups().stream().filter(i-> !"system".equals(i)).forEach(group->{

            String user = setting.get(group, "user");
            String pass = setting.get(group, "pass");
            new Thread(() -> {
                WatchVideo watchVideo = new WatchVideo(user,pass);
                try {
                    watchVideo.watch();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            },group).start();
        });
    }
}