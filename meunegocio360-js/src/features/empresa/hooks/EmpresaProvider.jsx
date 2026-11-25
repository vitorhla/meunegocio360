// ============================================================
// EmpresaProvider — provider da empresa ativa
// Rota de layout: renderiza <Outlet/> com o contexto.
// ============================================================
import { useEffect } from 'react';
import { Outlet } from 'react-router';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../../auth/index.js';
import { getEmpresa } from '../api/empresaApi.js';
import { EmpresaContext, EmpresaStatus } from './EmpresaContext.jsx';

/**
 * Provider da empresa ativa.
 *
 * Expõe { status, empresa, erro, temInativa, refetch }.
 * `temInativa` vem da extensão `existeInativa` do 404 COMPANY_NOT_REGISTERED:
 * true quando não há empresa ativa mas existe empresa inativa no sistema
 * (orienta a reativação nas telas de espera).
 * useEffect: quando a autenticação muda para não-autenticado,
 * remove os dados da empresa do cache (evita dados de outro
 * usuário no próximo login).
 */
export function EmpresaProvider() {
  const { status: authStatus } = useAuth();
  const queryClient = useQueryClient();

  const {
    data: empresa,
    isPending,
    isError,
    error,
    refetch,
  } = useQuery({
    queryKey: ['empresa'],
    queryFn: getEmpresa,
    retry: false,
  });

  // Limpa cache da empresa quando desautentica (evita dados de
  // outro usuário no próximo login — StrictMode é inofensivo
  // para query por key)
  useEffect(() => {
    if (authStatus !== 'autenticado') {
      queryClient.removeQueries({ queryKey: ['empresa'] });
    }
  }, [authStatus, queryClient]);

  // Deriva o status
  let status;
  if (isPending) {
    status = EmpresaStatus.CARREGANDO;
  } else if (isError) {
    status = error?.code === 'COMPANY_NOT_REGISTERED' ? EmpresaStatus.AUSENTE : EmpresaStatus.ERRO;
  } else {
    status = EmpresaStatus.ATIVA;
  }

  const value = {
    status,
    empresa: empresa || null,
    erro: error || null,
    temInativa:
      error?.code === 'COMPANY_NOT_REGISTERED' &&
      error.problem?.existeInativa === true,
    refetch,
    EmpresaStatus,
  };

  return (
    <EmpresaContext.Provider value={value}>
      <Outlet />
    </EmpresaContext.Provider>
  );
}
