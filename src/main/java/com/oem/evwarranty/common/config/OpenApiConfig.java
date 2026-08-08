package com.oem.evwarranty.common.config;

import com.oem.evwarranty.domain.user.Role;
import com.oem.evwarranty.domain.vehicle.Vehicle;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI evWarrantyOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("EV Warranty Management System API")
                                                .description(
                                                                "### Authenticate with Swagger\n" +
                                                                                "To test the APIs, use the **Authorize** button and sign in with one of the following accounts:\n"
                                                                                +
                                                                                "- **Admin**: `admin` / `password123` (Access to all endpoints)\n"
                                                                                +
                                                                                "- **SC Staff**: `scstaff` / `password123` (Access to Service Center /sc endpoints)\n"
                                                                                +
                                                                                "- **SC Technician**: `sctech` / `password123` (Access to Service Center /sc endpoints)\n"
                                                                                +
                                                                                "- **EVM Staff**: `evmstaff` / `password123` (Access to Manufacturer /evm endpoints)\n\n"
                                                                                +
                                                                                "Comprehensive API documentation for the OEM Electric Vehicle Warranty Management System.")
                                                .version("1.3.0")
                                                .contact(new Contact()
                                                                .name("OEM EV Warranty Support")
                                                                .email("support@oem-ev.com")
                                                                .url("https://oem-ev.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("http://springdoc.org")))
                                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                                .components(new io.swagger.v3.oas.models.Components()
                                                .addSecuritySchemes("basicAuth", new SecurityScheme()
                                                                .name("basicAuth")
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("basic")));
        }

        @Bean
        public GroupedOpenApi fullApi() {
                return GroupedOpenApi.builder()
                                .group("0-Full-Documentation")
                                .pathsToMatch("/**")
                                .build();
        }

        @Bean
        public GroupedOpenApi scStaffApi() {
                return GroupedOpenApi.builder()
                                .group("1-Service-Center-Operations")
                                .pathsToMatch("/sc/**")
                                .addOpenApiCustomizer(openApi -> openApi.getInfo()
                                                .setDescription("Required Roles: `SC_STAFF`, `SC_TECHNICIAN`, `ADMIN`"))
                                .build();
        }

        @Bean
        public GroupedOpenApi evmStaffApi() {
                return GroupedOpenApi.builder()
                                .group("2-Manufacturer-Operations")
                                .pathsToMatch("/evm/**")
                                .addOpenApiCustomizer(openApi -> openApi.getInfo()
                                                .setDescription("Required Roles: `EVM_STAFF`, `ADMIN`"))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminApi() {
                return GroupedOpenApi.builder()
                                .group("3-Admin-Operations")
                                .pathsToMatch("/admin/**")
                                .addOpenApiCustomizer(
                                                openApi -> openApi.getInfo().setDescription("Required Role: `ADMIN`"))
                                .build();
        }
}


