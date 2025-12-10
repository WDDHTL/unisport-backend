package com.unisport.config;


import com.unisport.properties.AliOssProperties;
import com.unisport.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
* 配置类：用于生成AliOssUtil对象
* */
@Configuration
@Slf4j
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean
     public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){
         log.info("开始创建阿里云文件上传客户端：{}",aliOssProperties);
         return new AliOssUtil(aliOssProperties.getEndpoint(),
                 aliOssProperties.getAccessKeyId(),
                 aliOssProperties.getAccessKeySecret(),
                 aliOssProperties.getBucketName());
     }
}
