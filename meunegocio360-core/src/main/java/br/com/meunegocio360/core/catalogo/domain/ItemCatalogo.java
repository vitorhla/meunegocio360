package br.com.meunegocio360.core.catalogo.domain;

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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Item do catálogo de produtos e serviços (RF-BE-005).
 *
 * <p>Produtos e serviços coexistem na mesma tabela, distinguidos pelo {@code tipo} (RN-02), que é
 * imutável após a criação. O item pertence à empresa ativa (RN-01, RNF-096); a categoria é opcional
 * e, quando informada, deve existir e estar ativa (RN-05). O {@code preco} é decimal com até 2
 * casas, não negativo (RN-03, RNF-033). A coluna {@code estoque_minimo} já existe no banco, mas
 * NENHUMA API/DTO a lê ou grava nesta fase (RN-07; será usada por Suprimentos).
 */
@Entity
@Table(name = "item_catalogo")
public class ItemCatalogo {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "empresa_id", nullable = false)
  private UUID empresaId;

  @Column(name = "categoria_id")
  private UUID categoriaId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private TipoItem tipo;

  @Column(length = 40)
  private String codigo;

  @Column(nullable = false, length = 150)
  private String nome;

  @Column(length = 500)
  private String descricao;

  @Column(nullable = false, length = 6)
  private String unidade;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal preco;

  // RN-07: existe no banco (DECIMAL(15,3), padrão 0) para uso futuro por Suprimentos; nenhum
  // DTO a recebe ou devolve nesta fase.
  @Column(name = "estoque_minimo", nullable = false, precision = 15, scale = 3)
  private BigDecimal estoqueMinimo;

  @Column(nullable = false)
  private boolean ativo;

  @Column(nullable = false)
  private Instant criadoEm;

  @Column(nullable = false)
  private Instant atualizadoEm;

  @Version
  @Column(nullable = false)
  private long versao;

  // Colunas geradas no banco (minúsculas, sem acento), usadas somente na busca (RN-10,
  // RNF-BE-26). Nulas no DDL por limitação do MariaDB 10.4; na prática refletem nome/codigo.
  @Column(name = "nome_busca", length = 150, insertable = false, updatable = false)
  private String nomeBusca;

  @Column(name = "codigo_busca", length = 40, insertable = false, updatable = false)
  private String codigoBusca;

  protected ItemCatalogo() {}

  public ItemCatalogo(
      UUID empresaId,
      UUID categoriaId,
      TipoItem tipo,
      String codigo,
      String nome,
      String descricao,
      String unidade,
      BigDecimal preco) {
    this.empresaId = empresaId;
    this.categoriaId = categoriaId;
    this.tipo = tipo;
    this.codigo = codigo;
    this.nome = nome;
    this.descricao = descricao;
    this.unidade = unidade;
    this.preco = preco;
    this.estoqueMinimo = BigDecimal.ZERO;
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

  /**
   * Edita os dados do item. O {@code tipo} recebido DEVE ser igual ao atual (RN-02 — violação
   * resulta em 422 ITEM_TYPE_IMMUTABLE, verificado no service).
   */
  public void atualizar(
      TipoItem tipo,
      String codigo,
      String nome,
      String descricao,
      String unidade,
      BigDecimal preco,
      UUID categoriaId) {
    this.tipo = tipo;
    this.codigo = codigo;
    this.nome = nome;
    this.descricao = descricao;
    this.unidade = unidade;
    this.preco = preco;
    this.categoriaId = categoriaId;
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

  public UUID getCategoriaId() {
    return categoriaId;
  }

  public TipoItem getTipo() {
    return tipo;
  }

  public String getCodigo() {
    return codigo;
  }

  public String getNome() {
    return nome;
  }

  public String getDescricao() {
    return descricao;
  }

  public String getUnidade() {
    return unidade;
  }

  public BigDecimal getPreco() {
    return preco;
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
