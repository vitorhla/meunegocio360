// ============================================================
// useClienteAcoes — mutações de clientes (cadastrar, atualizar,
// mudar situação, excluir) com invalidação do cache
// (RNF-FE-04)
// ============================================================
import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  cadastrarCliente,
  atualizarCliente,
  mudarSituacaoCliente,
  excluirCliente,
} from '../api/clientesApi.js';

export function useClienteAcoes() {
  const queryClient = useQueryClient();

  // Invalida a lista (todos os filtros/páginas) e a busca do seletor
  const invalidarClientes = () => {
    queryClient.invalidateQueries({ queryKey: ['clientes'] });
  };

  const cadastrar = useMutation({
    mutationFn: (payload) => cadastrarCliente(payload),
    onSuccess: invalidarClientes,
  });

  const atualizar = useMutation({
    mutationFn: ({ id, payload }) => atualizarCliente(id, payload),
    onSuccess: invalidarClientes,
  });

  const mudarSituacao = useMutation({
    mutationFn: ({ id, ativo, versao }) =>
      mudarSituacaoCliente(id, { ativo, versao }),
    onSuccess: invalidarClientes,
  });

  const excluir = useMutation({
    mutationFn: (id) => excluirCliente(id),
    onSuccess: invalidarClientes,
  });

  return { cadastrar, atualizar, mudarSituacao, excluir };
}
