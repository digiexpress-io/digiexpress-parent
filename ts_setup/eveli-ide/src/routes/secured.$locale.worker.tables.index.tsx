import { Box } from '@mui/material';
import { EveliTasksTable } from '@/eveli-tasks-2';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  return (
    <Box sx={{ p: 0.5 }}> {/* mock container / wrapper */}
      <EveliTasksTable />
    </Box>)
}
