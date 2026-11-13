// ============================================================
// App — bootstrap da aplicação (providers, roteador, layout)
// ============================================================
import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router';
import { AuthProvider, useAuth } from '../features/auth/index.js';
import LoadingSpinner from '../shared/ui/LoadingSpinner.jsx';
import ErrorScreen from '../shared/ui/ErrorScreen.jsx';
import RequireAuth from '../shared/ui/RequireAuth.jsx';

// Lazy loading das páginas (RNF-FE-05)
const LoginPage = lazy(() =>
  import('../features/auth/pages/LoginPage.jsx').then((m) => ({ default: m.default })),
);
const HomePage = lazy(() =>
  import('../features/auth/pages/HomePage.jsx').then((m) => ({ default: m.default })),
);
const NotFoundPage = lazy(() =>
  import('../features/auth/pages/NotFoundPage.jsx').then((m) => ({ default: m.default })),
);

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
              <Route
                path="/login"
                element={
                  <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
                    <LoginPage />
                  </Suspense>
                }
              />
              <Route
                path="/"
                element={
                  <RequireAuth>
                    <Suspense fallback={<LoadingSpinner text="Carregando…" />}>
                      <HomePage />
                    </Suspense>
                  </RequireAuth>
                }
              />
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
