package com.studora;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Studora API",
        description = "API para plataforma de estudos para concursos públicos",
        version = "1.0.0",
        contact = @Contact(
            name = "Suporte Studora",
            email = "suporte@example.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    )
)
public class StudoraApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudoraApplication.class, args);
    }
}
