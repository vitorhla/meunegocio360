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
                    {/* Apoio DEV — RF-FE-003 (registrada somente em DEV) */}
                    {import.meta.env.DEV && (
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
