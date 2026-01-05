package modelly.modelly_be.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI modellyAPI() {
        Info info = new Info()
                .title("Modelly API")
                .description("Modelly API 명세서")
                .version("1.0.0");

        String jwtSchemeName = "Bearer Authentication";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .addServersItem(new Server().url(contextPath))
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    @Bean
    public GroupedOpenApi designerApi() {
        return GroupedOpenApi.builder()
                .group("1. 디자이너 서비스") // 그룹 이름
                .pathsToMatch("/designers/**") // 해당 경로만 포함
                .build();
    }

    @Bean
    public GroupedOpenApi modelApi() {
        return GroupedOpenApi.builder()
                .group("1. 모델 서비스") // 그룹 이름
                .pathsToMatch("/models/**") // 해당 경로만 포함
                .build();
    }
}
