package us.martink.lunar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import us.martink.lunar.context.RequestContextFilter;

@SpringBootApplication
public class LunarApplication {
    public static void main(String[] args) {
        SpringApplication.run(LunarApplication.class, args);
    }

    @Bean
    public RequestContextFilter requestContext() {
        return new RequestContextFilter();
    }
}
