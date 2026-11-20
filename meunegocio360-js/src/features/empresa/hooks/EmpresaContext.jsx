// ============================================================
// EmpresaContext — contexto e hook da empresa
// Separado do provider para HMR (react-refresh)
// ============================================================
import { createContext, useContext } from 'react';

/** Estados possíveis da empresa */
export const EmpresaStatus = {
  CARREGANDO: 'carregando',
  ATIVA: 'ativa',
  AUSENTE: 'ausente',
  ERRO: 'erro',
};

export const EmpresaContext = createContext(null);

/**
 * Hook para consumir o contexto da empresa.
 */
export function useEmpresa() {
  const ctx = useContext(EmpresaContext);
  if (!ctx) {
    throw new Error('useEmpresa deve ser usado dentro de um EmpresaProvider');
  }
  return ctx;
}
