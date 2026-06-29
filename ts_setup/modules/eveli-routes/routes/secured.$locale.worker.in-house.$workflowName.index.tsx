import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { InHouseFill } from '../eveli-in-house';

export const Route = createFileRoute('/secured/$locale/worker/in-house/$workflowName/')({
  component: Component,
})

function Component() {
  const { workflowName } = Route.useParams();
  const navigate = useNavigate();

  function handleCancel() {
    navigate({ 
      from: '/secured/$locale/worker/in-house/$workflowName',
      to: '/secured/$locale/worker/in-house' });
  }

  return (<InHouseFill workflowName={workflowName} onCancel={handleCancel} />);
}
