package br.com.meunegocio360.core.auth.domain;

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

@Entity
@Table(name = "usuario")
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 120)
  private String nome;

  @Column(nullable = false, length = 160)
  private String email;

  @Column(nullable = false, length = 100)
  private String senhaHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Perfil perfil;

  @Column(nullable = false)
  private boolean ativo = true;

  @Column(nullable = false)
  private int tentativasFalhas;

  private Instant bloqueadoAte;

  private Instant ultimoLoginEm;

  @Column(nullable = false)
  private Instant criadoEm;

  @Column(nullable = false)
  private Instant atualizadoEm;

  @Version
  @Column(nullable = false)
  private long versao;

  protected Usuario() {}

  public Usuario(String nome, String email, String senhaHash, Perfil perfil) {
    this.nome = nome;
    this.email = email;
    this.senhaHash = senhaHash;
    this.perfil = perfil;
    this.ativo = true;
    this.tentativasFalhas = 0;
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

  public UUID getId() { return id; }
  public String getNome() { return nome; }
  public String getEmail() { return email; }
  public String getSenhaHash() { return senhaHash; }
  public Perfil getPerfil() { return perfil; }
  public boolean isAtivo() { return ativo; }
  public int getTentativasFalhas() { return tentativasFalhas; }
  public Instant getBloqueadoAte() { return bloqueadoAte; }
  public Instant getUltimoLoginEm() { return ultimoLoginEm; }
  public Instant getCriadoEm() { return criadoEm; }
  public Instant getAtualizadoEm() { return atualizadoEm; }
  public long getVersao() { return versao; }

  public void setAtivo(boolean ativo) { this.ativo = ativo; }

  public void registrarFalha(int maxTentativas, long bloqueioMinutos) {
    this.tentativasFalhas++;
    if (this.tentativasFalhas >= maxTentativas) {
      this.bloqueadoAte = Instant.now().plusSeconds(bloqueioMinutos * 60);
    }
  }

  public void registrarLoginSucesso() {
    this.tentativasFalhas = 0;
    this.bloqueadoAte = null;
    this.ultimoLoginEm = Instant.now();
  }

  public boolean estaBloqueado(Instant agora) {
    return bloqueadoAte != null && agora.isBefore(bloqueadoAte);
  }
}
