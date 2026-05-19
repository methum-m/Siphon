package com.methum.logstream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@Configuration
public class ReadAndWriteConfig {

    @Bean
    public ReentrantReadWriteLock readWriteLock(){
        return new ReentrantReadWriteLock();
    }
}
