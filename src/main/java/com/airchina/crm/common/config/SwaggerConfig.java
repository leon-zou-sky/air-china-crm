package com.airchina.crm.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Swagger 3 (OpenAPI) 配置
 *
 * 访问地址：
 * - Swagger UI: http://localhost:8080/crm/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/crm/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("凤凰知音 CRM 系统 API")
                        .version("1.0.0")
                        .description("国航凤凰知音高端旅客服务管理平台接口文档")
                        .contact(new Contact()
                                .name("CRM Team")
                                .email("crm@airchina.com"))
                        .license(new License()
                                .name("Internal Use Only")
                                .url("https://www.airchina.com")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080/crm")
                                .description("开发环境")
                ));
    }
}
