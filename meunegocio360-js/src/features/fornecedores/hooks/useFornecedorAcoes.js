// ============================================================
// useFornecedorAcoes — mutações de fornecedores (cadastrar,
// atualizar, mudar situação, excluir) com invalidação do
// cache (RNF-FE-04)
// ============================================================
import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  cadastrarFornecedor,
  atualizarFornecedor,
  mudarSituacaoFornecedor,
  excluirFornecedor,
} from '../api/fornecedoresApi.js';

export function useFornecedorAcoes() {
  const queryClient = useQueryClient();

  // Invalida a lista (todos os filtros/páginas) e a busca do seletor
  const invalidarFornecedores = () => {
    queryClient.invalidateQueries({ queryKey: ['fornecedores'] });
  };

  const cadastrar = useMutation({
    mutationFn: (payload) => cadastrarFornecedor(payload),
    onSuccess: invalidarFornecedores,
  });

  const atualizar = useMutation({
    mutationFn: ({ id, payload }) => atualizarFornecedor(id, payload),
    onSuccess: invalidarFornecedores,
  });

  const mudarSituacao = useMutation({
    mutationFn: ({ id, ativo, versao }) =>
      mudarSituacaoFornecedor(id, { ativo, versao }),
    onSuccess: invalidarFornecedores,
  });

  const excluir = useMutation({
    mutationFn: (id) => excluirFornecedor(id),
    onSuccess: invalidarFornecedores,
  });

  return { cadastrar, atualizar, mudarSituacao, excluir };
}
