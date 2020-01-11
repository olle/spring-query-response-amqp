package app;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@EnableScheduling
@SpringBootApplication
class Responding {

    @Scheduled(fixedDelay = Long.MAX_VALUE)
    void join() {

        // OK
    }


    public static void main(String[] args) {

        new SpringApplicationBuilder(Responding.class).web(WebApplicationType.NONE).run(args);
    }
}
