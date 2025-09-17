import { Box, Button, Container } from '@mui/material';
import { useIntl } from 'react-intl';
import { createFileRoute } from '@tanstack/react-router'
import { DialobReviewBasedOnForm } from '../dialob-review';
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { useFetch } from "@dxs-ts/envir-fetch";

import React from 'react';

export const Route = createFileRoute('/secured/$locale/worker/tasks/$taskId/review/')({
  component: Component,
})

function Component() {
  const intl = useIntl();
  const { taskId } = Route.useParams();
  const backend = useTaskBackend();

  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});
  const [task, setTask] = React.useState<TaskApi.Task>();


  React.useEffect(() => {
    getTask(taskId).then(setTask);
  }, [taskId]);

  async function handlePdfClick() {
    const url = await backend.persistence.getOneTaskPdfLink(task?.questionnaireId!, taskId);
    window.open(url);
  }
  return (
    <Container>
      <Button variant='outlined' onClick={handlePdfClick}>{intl.formatMessage({ id: 'taskLink.pdf.open' })}</Button>
      <DialobReviewBasedOnForm taskId={taskId} questionnaireId={''} onClose={() => { }} />
    </Container>
  )
}
