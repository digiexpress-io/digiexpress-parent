import React from 'react';

import { Button, Container } from '@mui/material';
import { useIntl } from 'react-intl';
import { createFileRoute } from '@tanstack/react-router'
import { DialobReviewBasedOnForm } from '../dialob-review';
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { useFetch } from "@dxs-ts/envir-fetch";


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
    const pdfBlob = await backend.persistence.getOneTaskPdf({ taskId, fields: [] });
    const pdfUrl = URL.createObjectURL(pdfBlob);
    const _newWindow = window.open(pdfUrl, '_blank');
  }
  
  return (
    <Container>
      <Button variant='outlined' onClick={handlePdfClick}>{intl.formatMessage({ id: 'taskLink.pdf.open' })}</Button>
      <DialobReviewBasedOnForm taskId={taskId} questionnaireId={''} onClose={() => { }} />
    </Container>
  )
}
