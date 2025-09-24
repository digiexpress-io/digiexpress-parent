import { Outlet, createFileRoute } from '@tanstack/react-router'
import { useSite } from '@dxs-ts/gamut-api';
import { GErrorNotTranslated } from '@dxs-ts/gamut-primitives';

export const Route = createFileRoute('/public/$locale/pages/$pageId')({
  component: Component,
})

function Component() {
  const { pageId } = Route.useParams()
  const { views } = useSite();
  const landingTopic = Object.values(views).find(a => a.id === pageId);

  if (landingTopic) {
    return <Outlet />
  }

  return (<GErrorNotTranslated />);
}

