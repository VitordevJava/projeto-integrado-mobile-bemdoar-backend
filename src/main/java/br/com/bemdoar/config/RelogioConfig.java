package br.com.bemdoar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class RelogioConfig {
    @Bean
    public Clock relogioBemDoar() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}
