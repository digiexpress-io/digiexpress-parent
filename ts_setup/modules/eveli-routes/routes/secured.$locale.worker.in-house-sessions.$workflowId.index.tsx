import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { InHouseFill } from '../eveli-in-house';

export const Route = createFileRoute('/secured/$locale/worker/in-house-sessions/$workflowId/')({
  component: Component,
})

function Component() {
  const { workflowId, locale } = Route.useParams();
  const navigate = useNavigate();

  function handleCancel() {
    navigate({ 
      from: '/secured/$locale/worker/in-house-sessions/$workflowId',
      to: '/secured/$locale/worker/in-house' });
  }

  return (<InHouseFill workflowId={workflowId} locale={locale} onCancel={handleCancel} />);
}
