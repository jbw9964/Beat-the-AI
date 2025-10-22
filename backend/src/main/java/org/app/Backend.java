package org.app;

import org.springframework.boot.*;
import org.springframework.data.jpa.repository.config.*;
import org.springframework.modulith.*;

@Modulith
@EnableJpaAuditing
public class Backend {

    public static void main(String[] args) {
        SpringApplication.run(Backend.class, args);
    }

}
