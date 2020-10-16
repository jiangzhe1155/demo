package org.jz.demo.spring;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jz.demo.spring.mapper.FileDO;
import org.jz.demo.spring.mapper.FileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    DataSource dataSource;

    @Autowired
    FileMapper fileMapper;

    @PostConstruct
    public void aa() {
        List<FileDO> fileDOS = fileMapper.selectList(new LambdaQueryWrapper<>());
        System.out.println(fileDOS);
    }
}
