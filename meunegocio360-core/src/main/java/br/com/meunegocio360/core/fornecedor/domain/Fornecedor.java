package br.com.meunegocio360.core.fornecedor.domain;

import br.com.meunegocio360.core.shared.domain.TipoPessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

/**
 * Fornecedor do sistema (RF-BE-004).
 *
 * <p>Todo fornecedor pertence à empresa ativa (RF-BE-003 RN-01, RNF-096): o {@code empresaId} é
 * resolvido no servidor e NUNCA vem do cliente. O documento (CPF ou CNPJ, somente dígitos) é único
 * por empresa, ativos e inativos (RF-BE-004 RN-02) — por entidade: o mesmo CNPJ PODE existir como
 * cliente e como fornecedor. O fornecedor com vínculo é inativado, nunca excluído (RF-BE-004 RN-03,
 * RNF-097).
 */
@Entity
@Table(name = "fornecedor")
public class Fornecedor {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "empresa_id", nullable = false)
  private UUID empresaId;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_pessoa", nullable = false, length = 10)
  private TipoPessoa tipoPessoa;

  @Column(nullable = false, length = 150)
  private String nome;

  @Column(nullable = false, length = 14)
  private String documento;

  @Column(length = 160)
  private String email;

  @Column(length = 11)
  private String telefone;

  @Column(nullable = false)
  private boolean ativo;

  @Column(nullable = false)
  private Instant criadoEm;

  @Column(nullable = false)
  private Instant atualizadoEm;

  @Version
  @Column(nullable = false)
  private long versao;

  // Coluna gerada no banco (minúsculas, sem acento), usada apenas na busca (RF-BE-003 RN-08,
  // RNF-BE-26). Nula no DDL por limitação do MariaDB 10.4 com colunas geradas; na prática nunca é
  // NULL.
  @Column(name = "nome_busca", length = 150, insertable = false, updatable = false)
  private String nomeBusca;

  protected Fornecedor() {}

  public Fornecedor(
      UUID empresaId,
      TipoPessoa tipoPessoa,
      String nome,
      String documento,
      String email,
      String telefone) {
    this.empresaId = empresaId;
    this.tipoPessoa = tipoPessoa;
    this.nome = nome;
    this.documento = documento;
    this.email = email;
    this.telefone = telefone;
    this.ativo = true;
  }

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    this.criadoEm = now;
    this.atualizadoEm = now;
  }

  @PreUpdate
  void preUpdate() {
    this.atualizadoEm = Instant.now();
  }

  /** Edita os dados do fornecedor, inclusive tipo e documento (RF-BE-003 RN-09). */
  public void atualizar(
      TipoPessoa tipoPessoa, String nome, String documento, String email, String telefone) {
    this.tipoPessoa = tipoPessoa;
    this.nome = nome;
    this.documento = documento;
    this.email = email;
    this.telefone = telefone;
  }

  public void inativar() {
    this.ativo = false;
  }

  public void reativar() {
    this.ativo = true;
  }

  public UUID getId() {
    return id;
  }

  public UUID getEmpresaId() {
    return empresaId;
  }

  public TipoPessoa getTipoPessoa() {
    return tipoPessoa;
  }

  public String getNome() {
    return nome;
  }

  public String getDocumento() {
    return documento;
  }

  public String getEmail() {
    return email;
  }

  public String getTelefone() {
    return telefone;
  }

  public boolean isAtivo() {
    return ativo;
  }

  public Instant getCriadoEm() {
    return criadoEm;
  }

  public Instant getAtualizadoEm() {
    return atualizadoEm;
  }

  public long getVersao() {
    return versao;
  }
}
