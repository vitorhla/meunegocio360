// ============================================================
// AppLayout — layout da aplicação (navbar, main, footer)
// ============================================================
import { NavLink, Outlet } from 'react-router';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { useAuth } from '../../features/auth/index.js';
import { useEmpresa } from '../../features/empresa/index.js';
import usePageTitle from '../../shared/lib/usePageTitle.js';
import brandIcon from '../../assets/icone_meunegocio360_branca.png';

/**
 * Layout principal da aplicação.
 * Exibe navbar com nome da empresa, menu por perfil, nome do
 * usuário e botão Sair. Footer com texto institucional.
 *
 * FE-003/004/005 acrescentam itens no menu.
 */
export default function AppLayout() {
  const { usuario, logout } = useAuth();
  const { empresa } = useEmpresa();
  usePageTitle(null); // Título base (cada página sobrescreve)

  const nomeEmpresa = empresa?.nomeFantasia || empresa?.razaoSocial || '';

  return (
    <>
      {/* Skip-link para navegação por teclado */}
      <a href="#conteudo" className="visually-hidden-focusable position-absolute start-0 z-3">
        Pular para o conteúdo
      </a>

      {/* Navbar laranja com texto preto (RNF-FE-2.2) */}
      <Navbar bg="primary" expand="md" className="py-2">
        <Container fluid>
          <Navbar.Brand className="fw-semibold text-nowrap me-3">
            <img
              src={brandIcon}
              alt=""
              aria-hidden="true"
              className="navbar-brand-logo me-2 align-middle"
            />
            MeuNegócio360
          </Navbar.Brand>

          {nomeEmpresa && (
            <span className="fw-medium text-truncate d-none d-md-inline-block me-auto empresa-navbar">
              {nomeEmpresa}
            </span>
          )}

          <Navbar.Toggle aria-controls="app-navbar-nav" />

          <Navbar.Collapse id="app-navbar-nav">
            <Nav className="ms-auto align-items-md-center gap-1">
              {/* Menu data-driven por perfil (RNF-FE-16) */}
              <Nav.Item>
                <NavLink to="/" end className="nav-link">
                  Início
                </NavLink>
              </Nav.Item>
              <Nav.Item>
                <NavLink to="/empresa" className="nav-link">
                  Empresa
                </NavLink>
              </Nav.Item>
              {/* FE-003: + Clientes */}
              {/* FE-004: + Fornecedores */}
              {/* FE-005: + Catálogo */}

              <span className="text-muted mx-1 small">{usuario?.nome}</span>

              <Button
                variant="outline-dark"
                size="sm"
                onClick={logout}
              >
                Sair
              </Button>
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>

      {/* Conteúdo principal */}
      <main id="conteudo" className="container py-4">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-footer py-3 text-center">
        <p className="mb-0 text-secondary small">
          MeuNegócio360 — Gestão para pequenos negócios
        </p>
      </footer>
    </>
  );
}
