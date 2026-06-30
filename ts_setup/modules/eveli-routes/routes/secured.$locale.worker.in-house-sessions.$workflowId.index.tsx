import React from 'react';
import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { InHouseFill, InHouseFillEnd } from '../eveli-in-house';

export const Route = createFileRoute('/secured/$locale/worker/in-house-sessions/$workflowId/')({
  component: Component,
})

function Component() {
  const { workflowId, locale } = Route.useParams();
  const navigate = useNavigate();
  const [completed, setCompleted] = React.useState(false);

  function handleCancel() {
    navigate({
      from: '/secured/$locale/worker/in-house-sessions/$workflowId',
      to: '/secured/$locale/worker/in-house'
    });
  }

  function handleComplete() {
    setCompleted(true);
  }

  function handleAccept() {
    navigate({
      from: '/secured/$locale/worker/in-house-sessions/$workflowId',
      to: '/secured/$locale/worker/in-house'
    });
  }

  if (completed) {
    return (<InHouseFillEnd onAccept={handleAccept} />);
  }

  return (<InHouseFill onComplete={handleComplete} workflowId={workflowId} locale={locale} onCancel={handleCancel} />);
}
