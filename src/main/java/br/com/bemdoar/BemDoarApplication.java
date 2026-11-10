package br.com.bemdoar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BemDoarApplication {

    public static void main(String[] args) {
        SpringApplication.run(BemDoarApplication.class, args);
    }
}
