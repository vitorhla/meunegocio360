// ============================================================
// useItemAcoes — mutações de itens do catálogo (cadastrar,
// atualizar, mudar situação, excluir) com invalidação do
// cache (RNF-FE-04)
// ============================================================
import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  cadastrarItem,
  atualizarItem,
  mudarSituacaoItem,
  excluirItem,
} from '../api/catalogoApi.js';

export function useItemAcoes() {
  const queryClient = useQueryClient();

  // Invalida a lista (todos os filtros/páginas) e a busca do seletor
  const invalidarItens = () => {
    queryClient.invalidateQueries({ queryKey: ['itens-catalogo'] });
  };

  const cadastrar = useMutation({
    mutationFn: (payload) => cadastrarItem(payload),
    onSuccess: invalidarItens,
  });

  const atualizar = useMutation({
    mutationFn: ({ id, payload }) => atualizarItem(id, payload),
    onSuccess: invalidarItens,
  });

  const mudarSituacao = useMutation({
    mutationFn: ({ id, ativo, versao }) =>
      mudarSituacaoItem(id, { ativo, versao }),
    onSuccess: invalidarItens,
  });

  const excluir = useMutation({
    mutationFn: (id) => excluirItem(id),
    onSuccess: invalidarItens,
  });

  return { cadastrar, atualizar, mudarSituacao, excluir };
}
