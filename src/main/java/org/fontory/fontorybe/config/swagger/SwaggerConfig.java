package org.fontory.fontorybe.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(info);
    }

    Info info = new Info().title("FONTORY APIS");
}
