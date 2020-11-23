package org.meeboo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.text.ParseException;

@Slf4j
@SpringBootApplication
public class MeebooRunner {

    public static void main(String[] args) throws ParseException {
        SpringApplication.run(MeebooRunner.class, args);
    }

}
