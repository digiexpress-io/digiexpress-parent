import { Container } from '@mui/material';
import { EveliTaskComposer } from '@/eveli-task-composer';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { taskId } = Route.useParams();

  return (
    <Container maxWidth='lg'>
      <EveliTaskComposer taskId={taskId}  />
    </Container>
  )
}
