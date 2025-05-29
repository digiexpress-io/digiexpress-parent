import { useIam } from '@/api-iam';
import { Navigate, Outlet, createFileRoute } from '@tanstack/react-router';
import { EveliErrorNotFound } from '../eveli-error-not-found/EveliErrorNotFound';

export const Route = createFileRoute('/secured/$locale')({
  component: Component,
  notFoundComponent: EveliErrorNotFound,
});

function Component() {
  const iam = useIam();

  if (iam.authType === 'ANON') {
    return (
      <Navigate
        from="/"
        to="/public/$locale/auth"
        params={{ locale: 'en' }}
      />
    );
  }

  return <Outlet />;
}
