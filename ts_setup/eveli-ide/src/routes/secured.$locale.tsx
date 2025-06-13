import { useIam } from '@/api-iam';
import { Navigate, Outlet } from '@tanstack/react-router';
import { EveliErrorNotFound } from '../eveli-error-not-found/EveliErrorNotFound';
import { useLocale } from '@/api-locale';

export const Route = createFileRoute({
  component: Component,
  notFoundComponent: EveliErrorNotFound,
});

function Component() {
  const iam = useIam();
  const { locale } = useLocale();
  
  if (iam.authType === 'ANON') {
    return (
      <Navigate
        from="/"
        to="/public/$locale/auth"
        params={{ locale }}
      />
    );
  }

  return <Outlet />;
}
