package com.application.boot.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author admin
 */
@SpringBootApplication(scanBasePackages = {"com.application.boot.*"},exclude = { DataSourceAutoConfiguration.class })
public class BaseBootApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(BaseBootApplication.class, args);
      
    }
}