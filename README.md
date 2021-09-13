![img](https://up.sowevo.com/img/bj_pc.png)

京训钉自动学习脚本
---

> 适用网站[北京市职业技能提升行动管理平台--京训钉](https://www.bjjnts.cn/)

> 网址<https://www.bjjnts.cn/>

> 运行环境Java+Chrome(理论上可以兼容其他浏览器)

### 功能

- 自动播放,自动换下一集...
- 自动处理验证码(百度api)
- 自动人脸识别

### 项目结构

![image-20210913155941935](https://up.sowevo.com/img/image-20210913155941935.png)

### 使用方式

- 下载代码,使用IDEA等开发工具打开

  ```shell
  $ git clone https://github.com/Sowevo/bjjnts.git
  ```

- 准备浏览器驱动

  项目中已经包含浏览器驱动,如果与自己浏览器版本不一致,请去这个地址[下载](https://sites.google.com/a/chromium.org/chromedriver/downloads)

  下载之后放到项目中`bjjnts/driver`目录下,**替换**原有文件

- 人脸识别处理

  手机自拍一段视频10S左右,并对视频进行转码

  > 使用ffmpeg进行转码命令

  ```shell
  # 源视频文件格式可以是 mov,mp4等格式
  # 生成视频后缀必须是y4m和wav,名称必须是你的手机号
  $ ffmpeg -y -i 源视频.mp4 -vf scale=960:540 -pix_fmt yuv420p 185XXXXXX404.y4m
  $ ffmpeg -y -i 源视频.mp4 -vn -acodec pcm_s16le -ar 48000 -ac 2 185XXXXXX404.wav
  ```

  将生成的两个文件放到项目中`bjjnts/face`目录中

  - 人脸识别效果验证

    在服务运行后弹出来的浏览器中,打开测试网站:https://webcamtests.com/

    检测是否正确加载你的视频

- 申请百度Ocr的api,每个月可以免费试用1000次

  1. [领取](https://console.bce.baidu.com/ai/#/ai/ocr/overview/resource/getFree)免费接口额度(不同的用户免费次数不一样)

  2. [创建应用](https://console.bce.baidu.com/ai/#/ai/ocr/overview/index)并获取API Key与Secret Key

  3. 使用命令获取**access_token**,[参考](https://ai.baidu.com/ai-doc/REFERENCE/Ck3dwjhhu)

     ```shell
     # 从返回值中找到access_token,格式类似
     # 24.6c5e1ff107f0e8bcef8c46d3424a0e78.2592000.1485516651.282335-8574074
     $ curl -i -k 'https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=【应用的API Key】&client_secret=【应用的Secret Key】'
     ```

  4. 把你的access_token填到`bjjnts/src/main/resources/application.yml`这个配置文件里.

- 填写你的用户信息到配置文件

  1. 打开`bjjnts/src/main/resources/application.yml`按照模板进行修改
  2. 需要修改默认密码,否则会报错
  3. 可以同时填多个用户信息,同时运行

- 运行`Main.java`


## 鸣谢
*   借鉴了[bjjnts](https://greasyfork.org/zh-CN/scripts/430451-bjjnts)项目的部分代码
