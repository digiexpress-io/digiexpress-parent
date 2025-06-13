import { Outlet } from '@tanstack/react-router'
import { useSite } from '../api-site';
import { GErrorNotTranslated } from '../g-error-not-translated/GErrorNotTranslated';

export const Route = createFileRoute({
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

