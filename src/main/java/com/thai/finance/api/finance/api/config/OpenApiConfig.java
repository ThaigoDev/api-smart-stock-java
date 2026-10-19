package com.thai.finance.api.finance.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentação interativa em /swagger-ui.html. Para chamar as rotas protegidas, obtenha um token em
 * POST /auth e informe-o no botão "Authorize".
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearer-jwt";

    @Bean
    public OpenAPI smartStockOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Stock API")
                        .version("1.0")
                        .description("API RESTful de controle de estoque para micro e pequenas empresas: cadastros, "
                                + "movimentações com bloqueio de estoque negativo, estorno, alertas de ruptura e "
                                + "superestoque, curva ABC, giro de estoque e conciliação de saldos."))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
