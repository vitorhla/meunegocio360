// ============================================================
// LoginForm — formulário de login com RHF + Zod (RNF-FE-20…26)
// ============================================================
import { useEffect, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate } from 'react-router';
import { Form, Button, Alert, Spinner, Card } from 'react-bootstrap';
import loginSchema from '../schemas.js';
import { login } from '../api/authApi.js';
import { useAuth } from '../hooks/AuthContext.jsx';

export default function LoginForm() {
  const { setSession } = useAuth();
  const navigate = useNavigate();

  const [serverError, setServerError] = useState(null);
  const [lockoutSeconds, setLockoutSeconds] = useState(0);
  const passwordRef = useRef(null);

  const {
    register,
    handleSubmit,
    setError,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', senha: '' },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
  });

  // Contagem regressiva para bloqueio
  useEffect(() => {
    if (lockoutSeconds <= 0) return;
    const id = setInterval(() => {
      setLockoutSeconds((prev) => Math.max(0, prev - 1));
    }, 1000);
    return () => clearInterval(id);
  }, [lockoutSeconds]);

  const isLocked = lockoutSeconds > 0;

  /**
   * Proteção contra redirecionamento aberto (ponto de atenção do requisito).
   * Só aceita caminhos internos começando com `/` (e não `//`).
   */
  const getSafeFrom = (from) => {
    if (typeof from === 'string' && from.startsWith('/') && !from.startsWith('//')) {
      return from;
    }
    return '/';
  };

  const onSubmit = async (data) => {
    setServerError(null);

    try {
      const result = await login(data.email, data.senha);

      // Sucesso: limpa formulário e redireciona
      reset({ email: '', senha: '' });
      setSession(result.accessToken, result.usuario, result.expiresIn);
      navigate(getSafeFrom(null), { replace: true });
    } catch (err) {
      // VALIDATION_ERROR → erros nos campos (400)
      if (err.code === 'VALIDATION_ERROR' && err.errors?.length) {
        for (const fe of err.errors) {
          setError(fe.field, { message: fe.message }, { shouldFocus: true });
        }
        return;
      }

      // INVALID_CREDENTIALS (401)
      if (err.code === 'INVALID_CREDENTIALS') {
        setServerError('E-mail ou senha inválidos.');
        // Limpa senha e foco no campo senha
        setTimeout(() => {
          if (passwordRef.current) {
            passwordRef.current.focus();
          }
        }, 100);
        return;
      }

      // ACCOUNT_TEMPORARILY_LOCKED (429)
      if (err.code === 'ACCOUNT_TEMPORARILY_LOCKED') {
        const retryAfter = err.retryAfter || 900; // fallback 15 min
        setLockoutSeconds(retryAfter);
        const minutos = Math.ceil(retryAfter / 60);
        setServerError(
          `Muitas tentativas. Tente novamente em ${minutos} minuto${minutos > 1 ? 's' : ''}.`,
        );
        return;
      }

      // Falha de rede / timeout / 5xx
      setServerError(
        'Não foi possível entrar agora. Verifique sua conexão e tente novamente.',
      );
    }
  };

  return (
    <Card
      className="shadow-sm"
      style={{ maxWidth: 420, width: '100%', margin: '0 auto' }}
    >
      <Card.Body className="p-4">
        <h1 className="fs-3 text-center mb-1">Entrar</h1>
        <p className="text-center text-secondary mb-4" style={{ fontSize: '0.95rem' }}>
          MeuNegócio360
        </p>

        {serverError && (
          <Alert variant="danger" role="alert" className="py-2 small">
            {serverError}
          </Alert>
        )}

        {/* eslint-disable-next-line react-hooks/refs */}
        <Form onSubmit={handleSubmit(onSubmit)} noValidate>
          {/* E-mail */}
          <Form.Group className="mb-3" controlId="login-email">
            <Form.Label>E-mail</Form.Label>
            <Form.Control
              type="email"
              autoComplete="username"
              placeholder="seu@email.com"
              isInvalid={!!errors.email}
              aria-describedby={errors.email ? 'login-email-feedback' : undefined}
              {...register('email')}
              disabled={isSubmitting || isLocked}
            />
            <Form.Control.Feedback type="invalid" id="login-email-feedback">
              {errors.email?.message}
            </Form.Control.Feedback>
          </Form.Group>

          {/* Senha */}
          <Form.Group className="mb-4" controlId="login-senha">
            <Form.Label>Senha</Form.Label>
            <Form.Control
              type="password"
              autoComplete="current-password"
              placeholder="Sua senha"
              isInvalid={!!errors.senha}
              aria-describedby={errors.senha ? 'login-senha-feedback' : undefined}
              {...register('senha')}
              disabled={isSubmitting || isLocked}
            />
            <Form.Control.Feedback type="invalid" id="login-senha-feedback">
              {errors.senha?.message}
            </Form.Control.Feedback>
          </Form.Group>

          <Button
            type="submit"
            variant="primary"
            className="w-100 d-flex align-items-center justify-content-center gap-2"
            disabled={isSubmitting || isLocked}
            style={{ minHeight: 48 }}
          >
            {isSubmitting && (
              <Spinner
                as="span"
                animation="border"
                size="sm"
                role="status"
                aria-hidden="true"
              />
            )}
            {isLocked
              ? `Aguarde ${formatLockout(lockoutSeconds)}`
              : isSubmitting
                ? 'Entrando…'
                : 'Entrar'}
          </Button>
        </Form>
      </Card.Body>
    </Card>
  );
}

function formatLockout(seconds) {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  if (m > 0) return `${m}min${s > 0 ? ` ${s}s` : ''}`;
  return `${s}s`;
}
