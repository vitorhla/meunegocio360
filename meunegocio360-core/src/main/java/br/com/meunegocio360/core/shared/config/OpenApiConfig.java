package br.com.meunegocio360.core.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentação OpenAPI/Swagger da API (springdoc).
 *
 * <p>Convenção do projeto: TODO endpoint novo DEVE ser documentado com
 * {@code @Tag}, {@code @Operation} e {@code @ApiResponse} (códigos de erro em
 * {@code application/problem+json}), e os DTOs de entrada/saída devem usar
 * {@code @Schema} nos campos.
 */
@Configuration
public class OpenApiConfig {

  private static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI meunegocio360CoreOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("MeuNegocio360 API — Núcleo")
                .description(
                    "Microsserviço Núcleo do MeuNegocio360. "
                        + "Autenticação por e-mail/senha com JWT de acesso (HS256) e refresh token "
                        + "rotativo em cookie HttpOnly. Erros em application/problem+json (RFC 9457) "
                        + "com o campo `code` para identificação do tipo de erro.")
                .version("v1")
                .license(new License().name("Proprietary")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
        .components(
            new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(
                    BEARER_SCHEME,
                    new SecurityScheme()
                        .name(BEARER_SCHEME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "Cole o JWT de acesso retornado no login: "
                                + "Authorization: Bearer <accessToken>")));
  }
}
