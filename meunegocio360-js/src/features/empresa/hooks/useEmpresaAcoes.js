// ============================================================
// useEmpresaAcoes — mutações da empresa (cadastrar, atualizar,
// mudar situação) com atualização do cache do TanStack Query
// ============================================================
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cadastrarEmpresa, atualizarEmpresa, mudarSituacao } from '../api/empresaApi.js';

/**
 * Hook com as 3 mutações da empresa.
 *
 * Cada mutação atualiza o cache do TanStack Query conforme a regra:
 * - cadastrar / atualizar sucesso -> setQueryData
 * - mudarSituacao com ativa=false -> invalidateQueries (refetch -> 404 -> ausente)
 * - mudarSituacao com ativa=true  -> setQueryData
 */
export function useEmpresaAcoes() {
  const queryClient = useQueryClient();

  const cadastrar = useMutation({
    mutationFn: (payload) => cadastrarEmpresa(payload),
    onSuccess: (data) => {
      queryClient.setQueryData(['empresa'], data);
    },
  });

  const atualizar = useMutation({
    mutationFn: ({ id, payload }) => atualizarEmpresa(id, payload),
    onSuccess: (data) => {
      queryClient.setQueryData(['empresa'], data);
    },
  });

  const mudarSituacaoMut = useMutation({
    mutationFn: ({ id, ativa, versao }) => mudarSituacao(id, ativa, versao),
    onSuccess: (data) => {
      if (data.ativa === false) {
        // Inativação -> refetch vira 404 -> estado ausente
        queryClient.invalidateQueries({ queryKey: ['empresa'] });
      } else {
        queryClient.setQueryData(['empresa'], data);
      }
      // A lista de empresas muda com a situação (usada na tela de reativação)
      queryClient.invalidateQueries({ queryKey: ['empresas'] });
    },
  });

  return {
    cadastrar,
    atualizar,
    mudarSituacao: mudarSituacaoMut,
  };
}
