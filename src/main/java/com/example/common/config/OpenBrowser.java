//package com.example.common.config;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class OpenBrowser implements CommandLineRunner {
//
//    private static final Logger logger = LoggerFactory.getLogger(OpenBrowser.class);
//
//    @Value("${open.browser.url}")
//    private String url;
//
//    @Override
//    public void run(String... args) throws Exception {
//        logger.info("项目启动成功……");
//        try {
//            // 可以指定自己的路径
//            Runtime.getRuntime().exec("cmd   /c   start   " + url);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//}

package com.example.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class OpenBrowser implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(OpenBrowser.class);

    @Value("${open.browser.url}")
    private String url;

    @Override
    public void run(String... args) throws Exception {
        logger.info("项目启动成功……");
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String command;

            if (os.contains("win")) {
                command = "cmd /c start " + url;
            } else if (os.contains("mac")) {
                command = "open " + url;
            } else {
                // add Linux support
                command = "xdg-open " + url;
            }

            Runtime.getRuntime().exec(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
