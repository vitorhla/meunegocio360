// ============================================================
// useCategoriaAcoes — mutações de categorias (criar, renomear,
// mudar situação, excluir) com invalidação do cache
// (RNF-FE-04)
// ============================================================
import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  criarCategoria,
  renomearCategoria,
  mudarSituacaoCategoria,
  excluirCategoria,
} from '../api/catalogoApi.js';

export function useCategoriaAcoes() {
  const queryClient = useQueryClient();

  const invalidarCategorias = () => {
    queryClient.invalidateQueries({ queryKey: ['categorias'] });
  };

  const criar = useMutation({
    mutationFn: (payload) => criarCategoria(payload),
    onSuccess: invalidarCategorias,
  });

  const renomear = useMutation({
    mutationFn: ({ id, nome, versao }) =>
      renomearCategoria(id, { nome, versao }),
    onSuccess: invalidarCategorias,
  });

  const mudarSituacao = useMutation({
    mutationFn: ({ id, ativo, versao }) =>
      mudarSituacaoCategoria(id, { ativo, versao }),
    onSuccess: invalidarCategorias,
  });

  const excluir = useMutation({
    mutationFn: (id) => excluirCategoria(id),
    onSuccess: invalidarCategorias,
  });

  return { criar, renomear, mudarSituacao, excluir };
}
