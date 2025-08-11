import { useIam } from '@dxs-ts/eveli-api';
import { Navigate, Outlet, createFileRoute } from '@tanstack/react-router';
import { EveliErrorNotFound } from '@dxs-ts/eveli-primitives';
import { useLocale } from '@dxs-ts/eveli-api';

export const Route = createFileRoute('/secured/$locale')({
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
