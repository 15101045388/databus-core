package com.blueline.databus.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@EnableAutoConfiguration
@ServletComponentScan
public class Application  {
	public static void main(String[] args) {
		// 启动应用程序
		SpringApplication.run(Application.class, args);
	}
}