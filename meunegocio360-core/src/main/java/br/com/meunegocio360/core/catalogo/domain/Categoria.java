package br.com.meunegocio360.core.catalogo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Categoria de itens do catálogo (RF-BE-005, RN-06).
 *
 * <p>Toda categoria pertence à empresa ativa (RN-01, RNF-096). O nome é único por empresa sem
 * distinguir maiúsculas e acentos (RN-06), garantido no banco pela coluna gerada {@code nome_busca}
 * (MariaDB 10.4 não possui collation insensível a acento — ver V7). A categoria com itens
 * vinculados não pode ser excluída, apenas inativada (RN-06).
 */
@Entity
@Table(name = "categoria")
public class Categoria {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "empresa_id", nullable = false)
  private UUID empresaId;

  @Column(nullable = false, length = 80)
  private String nome;

  @Column(nullable = false)
  private boolean ativo;

  @Column(nullable = false)
  private Instant criadoEm;

  @Column(nullable = false)
  private Instant atualizadoEm;

  @Version
  @Column(nullable = false)
  private long versao;

  // Coluna gerada no banco (minúsculas, sem acento), usada na unicidade e na busca (RN-06,
  // RNF-BE-26). Nula no DDL por limitação do MariaDB 10.4 com colunas geradas; na prática nunca
  // é NULL.
  @Column(name = "nome_busca", length = 80, insertable = false, updatable = false)
  private String nomeBusca;

  protected Categoria() {}

  public Categoria(UUID empresaId, String nome) {
    this.empresaId = empresaId;
    this.nome = nome;
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

  public void renomear(String nome) {
    this.nome = nome;
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

  public String getNome() {
    return nome;
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
