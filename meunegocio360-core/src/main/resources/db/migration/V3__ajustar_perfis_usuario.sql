-- RF-BE-002 (RN-07): o perfil USUARIO deixa de existir e passa a OPERADOR_COMERCIAL.
UPDATE usuario SET perfil = 'OPERADOR_COMERCIAL' WHERE perfil = 'USUARIO';
