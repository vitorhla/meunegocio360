// ============================================================
// App — bootstrap da aplicação (providers, roteador, layout)
// ============================================================
import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router';
import { AuthProvider, useAuth } from '../features/auth/index.js';
import LoadingSpinner from '../shared/ui/LoadingSpinner.jsx';
import ErrorScreen from '../shared/ui/ErrorScreen.jsx';
import RequireAuth from './guards/RequireAuth.jsx';
import RequireRole from './guards/RequireRole.jsx';
import { EmpresaProvider } from '../features/empresa/index.js';
import { Perfis } from '../shared/lib/perfis.js';
import RequireEmpresa from './guards/RequireEmpresa.jsx';
import AppLayout from './layout/AppLayout.jsx';

// Lazy loading das páginas (RNF-FE-05)
const LoginPage = lazy(() =>
  import('../features/auth/pages/LoginPage.jsx').then((m) => ({ default: m.default })),
);
const HomePage = lazy(() =>
  import('../features/auth/pages/HomePage.jsx').then((m) => ({ default: m.default })),
);
const EmpresaPage = lazy(() =>
  import('../features/empresa/pages/EmpresaPage.jsx').then((m) => ({ default: m.default })),
);
const CadastroEmpresaPage = lazy(() =>
  import('../features/empresa/pages/CadastroEmpresaPage.jsx').then((m) => ({ default: m.default })),
);
const NotFoundPage = lazy(() =>
  import('../features/auth/pages/NotFoundPage.jsx').then((m) => ({ default: m.default })),
);
const ClientesPage = lazy(() =>
  import('../features/clientes/pages/ClientesPage.jsx').then((m) => ({ default: m.default })),
);
const NovoClientePage = lazy(() =>
  import('../features/clientes/pages/NovoClientePage.jsx').then((m) => ({ default: m.default })),
);
const EditarClientePage = lazy(() =>
  import('../features/clientes/pages/EditarClientePage.jsx').then((m) => ({ default: m.default })),
);
const ClienteSelectDevPage = lazy(() =>
  import('../features/clientes/pages/ClienteSelectDevPage.jsx').then((m) => ({ default: m.default })),
);
const FornecedoresPage = lazy(() =>
  import('../features/fornecedores/pages/FornecedoresPage.jsx').then((m) => ({ default: m.default })),
);
const NovoFornecedorPage = lazy(() =>
  import('../features/fornecedores/pages/NovoFornecedorPage.jsx').then((m) => ({ default: m.default })),
);
const EditarFornecedorPage = lazy(() =>
  import('../features/fornecedores/pages/EditarFornecedorPage.jsx').then((m) => ({ default: m.default })),
);
const FornecedorSelectDevPage = lazy(() =>
  import('../features/fornecedores/pages/FornecedorSelectDevPage.jsx').then((m) => ({ default: m.default })),
);
const CatalogoPage = lazy(() =>
  import('../features/catalogo/pages/CatalogoPage.jsx').then((m) => ({ default: m.default })),
);
const NovoItemPage = lazy(() =>
  import('../features/catalogo/pages/NovoItemPage.jsx').then((m) => ({ default: m.default })),
);
const EditarItemPage = lazy(() =>
  import('../features/catalogo/pages/EditarItemPage.jsx').then((m) => ({ default: m.default })),
);
const CategoriasPage = lazy(() =>
  import('../features/catalogo/pages/CategoriasPage.jsx').then((m) => ({ default: m.default })),
);
const ItemSelectDevPage = lazy(() =>
  import('../features/catalogo/pages/ItemSelectDevPage.jsx').then((m) => ({ default: m.default })),
);

/**
 * Roteador padrão das rotas de clientes (RF-FE-003):
 * perfil (ADMIN/OPERADOR_COMERCIAL) + empresa ativa.
 */
function RotasClientes({ children }) {
  return (
    <RequireRole roles={[Perfis.ADMIN, Perfis.OPERADOR_COMERCIAL]}>
      <RequireEmpresa>
        <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
          {children}
        </Suspense>
      </RequireEmpresa>
    </RequireRole>
  );
}

/**
 * Roteador padrão das rotas de fornecedores (RF-FE-004):
 * perfil (ADMIN/OPERADOR_FINANCEIRO) + empresa ativa.
 */
function RotasFornecedores({ children }) {
  return (
    <RequireRole roles={[Perfis.ADMIN, Perfis.OPERADOR_FINANCEIRO]}>
      <RequireEmpresa>
        <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
          {children}
        </Suspense>
      </RequireEmpresa>
    </RequireRole>
  );
}

/**
 * Roteador padrão das rotas do catálogo (RF-FE-005):
 * perfil (ADMIN/OPERADOR_COMERCIAL) + empresa ativa.
 */
function RotasCatalogo({ children }) {
  return (
    <RequireRole roles={[Perfis.ADMIN, Perfis.OPERADOR_COMERCIAL]}>
      <RequireEmpresa>
        <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
          {children}
        </Suspense>
      </RequireEmpresa>
    </RequireRole>
  );
}

function AuthGate({ children }) {
  const { status, sessionError, retryRestore } = useAuth();

  if (status === 'carregando') {
    return <LoadingSpinner text="Carregando…" />;
  }

  if (status === 'erro') {
    return (
      <ErrorScreen
        message={sessionError || 'Não foi possível conectar ao servidor.'}
        onRetry={retryRestore}
      />
    );
  }

  return children;
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AuthGate>
          <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
            <Routes>
              {/* Login (público) */}
              <Route
                path="/login"
                element={
                  <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
                    <LoginPage />
                  </Suspense>
                }
              />

              {/* Rotas protegidas (autenticadas) */}
              <Route element={<RequireAuth />}>
                <Route element={<EmpresaProvider />}>
                  <Route element={<AppLayout />}>
                    {/* Início — exige empresa ativa */}
                    <Route
                      index
                      element={
                        <RequireEmpresa>
                          <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
                            <HomePage />
                          </Suspense>
                        </RequireEmpresa>
                      }
                    />
                    {/* Empresa (consulta/edição) — acesso "Autenticado" (tabela
                        de rotas do RF-FE-002); a EmpresaPage trata 'ausente' */}
                    <Route
                      path="empresa"
                      element={
                        <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
                          <EmpresaPage />
                        </Suspense>
                      }
                    />
                    {/* Cadastro da empresa — ADMIN, sem empresa ativa */}
                    <Route
                      path="empresa/cadastro"
                      element={
                        <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
                          <RequireRole roles={[Perfis.ADMIN]}>
                            <CadastroEmpresaPage />
                          </RequireRole>
                        </Suspense>
                      }
                    />
                    {/* Clientes — RF-FE-003 (ADMIN e OPERADOR_COMERCIAL) */}
                    <Route
                      path="clientes"
                      element={
                        <RotasClientes>
                          <ClientesPage />
                        </RotasClientes>
                      }
                    />
                    <Route
                      path="clientes/novo"
                      element={
                        <RotasClientes>
                          <NovoClientePage />
                        </RotasClientes>
                      }
                    />
                    <Route
                      path="clientes/:id"
                      element={
                        <RotasClientes>
                          <EditarClientePage />
                        </RotasClientes>
                      }
                    />
                    {/* Fornecedores — RF-FE-004 (ADMIN e OPERADOR_FINANCEIRO) */}
                    <Route
                      path="fornecedores"
                      element={
                        <RotasFornecedores>
                          <FornecedoresPage />
                        </RotasFornecedores>
                      }
                    />
                    <Route
                      path="fornecedores/novo"
                      element={
                        <RotasFornecedores>
                          <NovoFornecedorPage />
                        </RotasFornecedores>
                      }
                    />
                    <Route
                      path="fornecedores/:id"
                      element={
                        <RotasFornecedores>
                          <EditarFornecedorPage />
                        </RotasFornecedores>
                      }
                    />
                    {/* Catálogo — RF-FE-005 (ADMIN e OPERADOR_COMERCIAL) */}
                    <Route
                      path="catalogo"
                      element={
                        <RotasCatalogo>
                          <CatalogoPage />
                        </RotasCatalogo>
                      }
                    />
                    <Route
                      path="catalogo/novo"
                      element={
                        <RotasCatalogo>
                          <NovoItemPage />
                        </RotasCatalogo>
                      }
                    />
                    <Route
                      path="catalogo/categorias"
                      element={
                        <RotasCatalogo>
                          <CategoriasPage />
                        </RotasCatalogo>
                      }
                    />
                    <Route
                      path="catalogo/:id"
                      element={
                        <RotasCatalogo>
                          <EditarItemPage />
                        </RotasCatalogo>
                      }
                    />
                    {/* Apoio DEV — RF-FE-003/004/005 (registrado somente em DEV) */}
                    {import.meta.env.DEV && (
                      <>
                        <Route
                          path="dev/cliente-select"
                          element={
                            <RequireRole
                              roles={[Perfis.ADMIN, Perfis.OPERADOR_COMERCIAL]}
                            >
                              <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
                                <ClienteSelectDevPage />
                              </Suspense>
                            </RequireRole>
                          }
                        />
                        <Route
                          path="dev/fornecedor-select"
                          element={
                            <RequireRole
                              roles={[Perfis.ADMIN, Perfis.OPERADOR_FINANCEIRO]}
                            >
                              <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
                                <FornecedorSelectDevPage />
                              </Suspense>
                            </RequireRole>
                          }
                        />
                        <Route
                          path="dev/item-select"
                          element={
                            <RequireRole
                              roles={[Perfis.ADMIN, Perfis.OPERADOR_COMERCIAL]}
                            >
                              <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
                                <ItemSelectDevPage />
                              </Suspense>
                            </RequireRole>
                          }
                        />
                      </>
                    )}
                  </Route>
                </Route>
              </Route>

              {/* 404 */}
              <Route
                path="*"
                element={
                  <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
                    <NotFoundPage />
                  </Suspense>
                }
              />
            </Routes>
          </Suspense>
        </AuthGate>
      </AuthProvider>
    </BrowserRouter>
  );
}
