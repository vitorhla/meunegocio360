package br.com.meunegocio360.core.shared.exception;

import br.com.meunegocio360.core.auth.application.AuthService.AccountLockedException;
import br.com.meunegocio360.core.auth.application.AuthService.InvalidCredentialsException;
import br.com.meunegocio360.core.auth.application.AuthService.InvalidRefreshTokenException;
import br.com.meunegocio360.core.auth.application.AuthService.OriginNotAllowedException;
import br.com.meunegocio360.core.cliente.application.ClienteService;
import br.com.meunegocio360.core.empresa.application.EmpresaAtiva;
import br.com.meunegocio360.core.empresa.application.EmpresaService;
import br.com.meunegocio360.core.fornecedor.application.FornecedorService;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String COOKIE_NAME = "mn360_refresh";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(
      MethodArgumentNotValidException ex, WebRequest request) {
    List<FieldErrorBean> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new FieldErrorBean(fe.getField(), fe.getDefaultMessage()))
            .toList();

    ProblemDetail problem =
        createProblem(
            HttpStatus.BAD_REQUEST,
            "https://meunegocio360.com.br/problemas/validacao",
            "Dados inválidos",
            "Um ou mais campos são inválidos.",
            request,
            "VALIDATION_ERROR");
    problem.setProperty("errors", errors);
    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleMalformedJson(
      HttpMessageNotReadableException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.BAD_REQUEST,
            "https://meunegocio360.com.br/problemas/validacao",
            "Dados inválidos",
            "JSON malformado ou tipo incompatível.",
            request,
            "VALIDATION_ERROR");
    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ProblemDetail> handleInvalidCredentials(
      InvalidCredentialsException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.UNAUTHORIZED,
            "https://meunegocio360.com.br/problemas/credenciais-invalidas",
            "Credenciais inválidas",
            "E-mail ou senha inválidos.",
            request,
            "INVALID_CREDENTIALS");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
  }

  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<ProblemDetail> handleInvalidRefreshToken(
      InvalidRefreshTokenException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.UNAUTHORIZED,
            "https://meunegocio360.com.br/problemas/refresh-token-invalido",
            "Refresh token inválido",
            "Refresh token inválido, expirado ou revogado.",
            request,
            "INVALID_REFRESH_TOKEN");

    String expiredCookie =
        org.springframework.http.ResponseCookie.from(COOKIE_NAME, "")
            .httpOnly(true)
            .path("/api/v1/auth")
            .maxAge(0)
            .build()
            .toString();
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .header(HttpHeaders.SET_COOKIE, expiredCookie)
        .body(problem);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ProblemDetail> handleBadCredentials(
      BadCredentialsException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.UNAUTHORIZED,
            "https://meunegocio360.com.br/problemas/nao-autorizado",
            "Não autenticado",
            "Token de acesso ausente, inválido ou expirado.",
            request,
            "UNAUTHORIZED");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
        .body(problem);
  }

  @ExceptionHandler(OriginNotAllowedException.class)
  public ResponseEntity<ProblemDetail> handleOriginNotAllowed(
      OriginNotAllowedException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.FORBIDDEN,
            "https://meunegocio360.com.br/problemas/origem-nao-permitida",
            "Origem não permitida",
            "Origem não permitida.",
            request,
            "ORIGIN_NOT_ALLOWED");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
  }

  @ExceptionHandler(AccountLockedException.class)
  public ResponseEntity<ProblemDetail> handleAccountLocked(
      AccountLockedException ex, WebRequest request) {
    long retryAfter =
        Math.max(0, Duration.between(Instant.now(), ex.getBloqueadoAte()).getSeconds());

    ProblemDetail problem =
        createProblem(
            HttpStatus.TOO_MANY_REQUESTS,
            "https://meunegocio360.com.br/problemas/conta-bloqueada",
            "Conta temporariamente bloqueada",
            "Conta temporariamente bloqueada por tentativas excessivas.",
            request,
            "ACCOUNT_TEMPORARILY_LOCKED");
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter))
        .body(problem);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleAccessDenied(
      AccessDeniedException ex, WebRequest request) {
    // Denegações de @PreAuthorize (RNF-BE-47) chegam até aqui; sem um handler específico,
    // o handler genérico responderia 500 em vez de 403.
    ProblemDetail problem =
        createProblem(
            HttpStatus.FORBIDDEN,
            "https://meunegocio360.com.br/problemas/sem-permissao",
            "Sem permissão",
            "Você não tem permissão para executar esta operação.",
            request,
            "ACCESS_DENIED");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
  }

  @ExceptionHandler(EmpresaService.CompanyNotRegisteredException.class)
  public ResponseEntity<ProblemDetail> handleCompanyNotRegistered(
      EmpresaService.CompanyNotRegisteredException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.NOT_FOUND,
            "https://meunegocio360.com.br/problemas/empresa-nao-cadastrada",
            "Empresa não cadastrada",
            ex.getMessage(),
            request,
            "COMPANY_NOT_REGISTERED");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
  }

  @ExceptionHandler(EmpresaService.CompanyCnpjDuplicateException.class)
  public ResponseEntity<ProblemDetail> handleCompanyCnpjDuplicate(
      EmpresaService.CompanyCnpjDuplicateException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.CONFLICT,
            "https://meunegocio360.com.br/problemas/empresa-cnpj-duplicado",
            "CNPJ já cadastrado",
            ex.getMessage(),
            request,
            "COMPANY_CNPJ_DUPLICATE");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(EmpresaService.CompanyAlreadyActiveException.class)
  public ResponseEntity<ProblemDetail> handleCompanyAlreadyActive(
      EmpresaService.CompanyAlreadyActiveException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.CONFLICT,
            "https://meunegocio360.com.br/problemas/empresa-ja-ativa",
            "Já existe empresa ativa",
            ex.getMessage(),
            request,
            "COMPANY_ALREADY_ACTIVE");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(EmpresaAtiva.CompanyNotActiveException.class)
  public ResponseEntity<ProblemDetail> handleCompanyNotActive(
      EmpresaAtiva.CompanyNotActiveException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://meunegocio360.com.br/problemas/empresa-nao-ativa",
            "Empresa não ativa",
            ex.getMessage(),
            request,
            "COMPANY_NOT_ACTIVE");
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
  }

  @ExceptionHandler(ClienteService.CustomerNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleCustomerNotFound(
      ClienteService.CustomerNotFoundException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.NOT_FOUND,
            "https://meunegocio360.com.br/problemas/cliente-nao-encontrado",
            "Cliente não encontrado",
            ex.getMessage(),
            request,
            "CUSTOMER_NOT_FOUND");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
  }

  @ExceptionHandler(ClienteService.CustomerDocumentDuplicateException.class)
  public ResponseEntity<ProblemDetail> handleCustomerDocumentDuplicate(
      ClienteService.CustomerDocumentDuplicateException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.CONFLICT,
            "https://meunegocio360.com.br/problemas/cliente-documento-duplicado",
            "Documento já cadastrado",
            ex.getMessage(),
            request,
            "CUSTOMER_DOCUMENT_DUPLICATE");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(ClienteService.CustomerHasHistoryException.class)
  public ResponseEntity<ProblemDetail> handleCustomerHasHistory(
      ClienteService.CustomerHasHistoryException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://meunegocio360.com.br/problemas/cliente-com-historico",
            "Cliente não pode ser excluído",
            ex.getMessage(),
            request,
            "CUSTOMER_HAS_HISTORY");
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
  }

  @ExceptionHandler(ClienteService.InvalidQueryException.class)
  public ResponseEntity<ProblemDetail> handleInvalidQuery(
      ClienteService.InvalidQueryException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.BAD_REQUEST,
            "https://meunegocio360.com.br/problemas/validacao",
            "Dados inválidos",
            ex.getMessage(),
            request,
            "VALIDATION_ERROR");
    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(FornecedorService.SupplierNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleSupplierNotFound(
      FornecedorService.SupplierNotFoundException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.NOT_FOUND,
            "https://meunegocio360.com.br/problemas/fornecedor-nao-encontrado",
            "Fornecedor não encontrado",
            ex.getMessage(),
            request,
            "SUPPLIER_NOT_FOUND");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
  }

  @ExceptionHandler(FornecedorService.SupplierDocumentDuplicateException.class)
  public ResponseEntity<ProblemDetail> handleSupplierDocumentDuplicate(
      FornecedorService.SupplierDocumentDuplicateException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.CONFLICT,
            "https://meunegocio360.com.br/problemas/fornecedor-documento-duplicado",
            "Documento já cadastrado",
            ex.getMessage(),
            request,
            "SUPPLIER_DOCUMENT_DUPLICATE");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(FornecedorService.SupplierHasHistoryException.class)
  public ResponseEntity<ProblemDetail> handleSupplierHasHistory(
      FornecedorService.SupplierHasHistoryException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://meunegocio360.com.br/problemas/fornecedor-com-historico",
            "Fornecedor não pode ser excluído",
            ex.getMessage(),
            request,
            "SUPPLIER_HAS_HISTORY");
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
  }

  @ExceptionHandler(FornecedorService.InvalidQueryException.class)
  public ResponseEntity<ProblemDetail> handleSupplierInvalidQuery(
      FornecedorService.InvalidQueryException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.BAD_REQUEST,
            "https://meunegocio360.com.br/problemas/validacao",
            "Dados inválidos",
            ex.getMessage(),
            request,
            "VALIDATION_ERROR");
    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, WebRequest request) {
    // Parâmetro de URL/query com tipo errado (ex.: size=abc) é entrada do cliente, não erro
    // interno (RNF-BE-14/15).
    ProblemDetail problem =
        createProblem(
            HttpStatus.BAD_REQUEST,
            "https://meunegocio360.com.br/problemas/validacao",
            "Dados inválidos",
            "Parâmetro '" + ex.getName() + "' com valor inválido.",
            request,
            "VALIDATION_ERROR");
    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<ProblemDetail> handleVersionConflict(
      ObjectOptimisticLockingFailureException ex, WebRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.CONFLICT,
            "https://meunegocio360.com.br/problemas/conflito-de-versao",
            "Conflito de versão",
            "A versão informada está desatualizada. Recarregue os dados e tente novamente.",
            request,
            "VERSION_CONFLICT");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> handleDataIntegrity(
      DataIntegrityViolationException ex, WebRequest request) {
    // Violações de integridade do banco NUNCA chegam ao cliente como 500 genérico
    // (RNF-BE-15): traduzimos para o erro de negócio correspondente.
    String detalhe =
        ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null
            ? ex.getMostSpecificCause().getMessage()
            : String.valueOf(ex.getMessage());
    if (detalhe.contains("uk_empresa_cnpj")) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              createProblem(
                  HttpStatus.CONFLICT,
                  "https://meunegocio360.com.br/problemas/empresa-cnpj-duplicado",
                  "CNPJ já cadastrado",
                  "CNPJ já cadastrado em outra empresa.",
                  request,
                  "COMPANY_CNPJ_DUPLICATE"));
    }
    if (detalhe.contains("uk_empresa_ativa_unica")) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              createProblem(
                  HttpStatus.CONFLICT,
                  "https://meunegocio360.com.br/problemas/empresa-ja-ativa",
                  "Já existe empresa ativa",
                  "Já existe uma empresa ativa.",
                  request,
                  "COMPANY_ALREADY_ACTIVE"));
    }
    if (detalhe.contains("uk_cliente_empresa_documento")) {
      // Corrida na verificação de unicidade do documento do cliente (RN-03): o índice único
      // no banco é a fonte da verdade.
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              createProblem(
                  HttpStatus.CONFLICT,
                  "https://meunegocio360.com.br/problemas/cliente-documento-duplicado",
                  "Documento já cadastrado",
                  "Cliente com este documento já existe (pode estar inativo).",
                  request,
                  "CUSTOMER_DOCUMENT_DUPLICATE"));
    }
    if (detalhe.contains("uk_fornecedor_empresa_documento")) {
      // Corrida na verificação de unicidade do documento do fornecedor (RF-BE-004 RN-02): o
      // índice único no banco é a fonte da verdade.
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              createProblem(
                  HttpStatus.CONFLICT,
                  "https://meunegocio360.com.br/problemas/fornecedor-documento-duplicado",
                  "Documento já cadastrado",
                  "Fornecedor com este documento já existe (pode estar inativo).",
                  request,
                  "SUPPLIER_DOCUMENT_DUPLICATE"));
    }
    log.error("Violação de integridade do banco não mapeada", ex);
    ProblemDetail problem =
        createProblem(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "https://meunegocio360.com.br/problemas/erro-interno",
            "Erro interno",
            "Ocorreu um erro inesperado.",
            request,
            "INTERNAL_ERROR");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, WebRequest request) {
    log.error("Erro interno inesperado", ex);
    ProblemDetail problem =
        createProblem(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "https://meunegocio360.com.br/problemas/erro-interno",
            "Erro interno",
            "Ocorreu um erro inesperado.",
            request,
            "INTERNAL_ERROR");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }

  public String buildUnauthorized(
      jakarta.servlet.http.HttpServletRequest request,
      org.springframework.security.core.AuthenticationException authException) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.UNAUTHORIZED,
            "https://meunegocio360.com.br/problemas/nao-autorizado",
            "Não autenticado",
            "Token de acesso ausente, inválido ou expirado.",
            request,
            "UNAUTHORIZED");
    problem.setProperty("traceId", resolveTraceId());
    return toJson(problem);
  }

  public String buildForbidden(jakarta.servlet.http.HttpServletRequest request) {
    ProblemDetail problem =
        createProblem(
            HttpStatus.FORBIDDEN,
            "https://meunegocio360.com.br/problemas/sem-permissao",
            "Sem permissão",
            "Você não tem permissão para executar esta operação.",
            request,
            "ACCESS_DENIED");
    problem.setProperty("traceId", resolveTraceId());
    return toJson(problem);
  }

  private ProblemDetail createProblem(
      HttpStatusCode status,
      String type,
      String title,
      String detail,
      WebRequest request,
      String code) {
    ProblemDetail problem = ProblemDetail.forStatus(status);
    problem.setType(URI.create(type));
    problem.setTitle(title);
    problem.setDetail(detail);
    problem.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
    problem.setProperty("code", code);
    problem.setProperty("traceId", resolveTraceId());
    return problem;
  }

  private ProblemDetail createProblem(
      HttpStatusCode status,
      String type,
      String title,
      String detail,
      jakarta.servlet.http.HttpServletRequest request,
      String code) {
    ProblemDetail problem = ProblemDetail.forStatus(status);
    problem.setType(URI.create(type));
    problem.setTitle(title);
    problem.setDetail(detail);
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("code", code);
    return problem;
  }

  private String resolveTraceId() {
    return Optional.ofNullable(MDC.get("traceId")).orElse("unknown");
  }

  private String toJson(ProblemDetail problem) {
    try {
      return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(problem);
    } catch (Exception e) {
      return "{\"title\":\"Erro interno\",\"status\":500}";
    }
  }

  private record FieldErrorBean(String field, String message) {}
}
