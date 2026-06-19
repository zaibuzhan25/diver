package com.travel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 项目根目录：user.dir 在 IDEA 中通常是 src/backend，需要退到项目根目录
        String userDir = System.getProperty("user.dir").replace("\\", "/");

        // 如果在 src/backend 里启动，退到项目根目录
        if (userDir.endsWith("/src/backend")) {
            userDir = userDir.substring(0, userDir.length() - "/src/backend".length());
        }

        // 前端文件实际目录
        File frontendDir = new File(userDir + "/src/frontend");
        String frontendPath = "file:///" + frontendDir.getAbsolutePath().replace("\\", "/");

        System.out.println("[WebConfig] frontendDir.exists() = " + frontendDir.exists());
        System.out.println("[WebConfig] 映射 /src/frontend/** -> " + frontendPath);

        registry.addResourceHandler("/src/frontend/**")
                .addResourceLocations(frontendPath);
    }
}
