import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { InHouseFillStart } from '../eveli-in-house';

export const Route = createFileRoute('/secured/$locale/worker/in-house/$workflowName/')({
  component: Component,
})

function Component() {
  const { workflowName, locale } = Route.useParams();
  const navigate = useNavigate();

  function handleCancel() {
    navigate({ 
      from: '/secured/$locale/worker/in-house/$workflowName',
      to: '/secured/$locale/worker/in-house' });
  }

  return (<InHouseFillStart workflowName={workflowName} locale={locale} onCancel={handleCancel} />);
}
