// ============================================================
// AuthContext — contexto e hook de autenticação
// Separado do provider para HMR (react-refresh)
// ============================================================
import { createContext, useContext } from 'react';

/** Estados possíveis do AuthProvider */
export const AuthStatus = {
  CARREGANDO: 'carregando',
  AUTENTICADO: 'autenticado',
  ANONIMO: 'anonimo',
  ERRO: 'erro',
};

export const AuthContext = createContext(null);

/**
 * Hook para consumir o contexto de autenticação.
 */
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider');
  }
  return ctx;
}
