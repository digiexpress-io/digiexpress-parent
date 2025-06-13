import React from 'react'
import { Box } from '@mui/material';
import { useLocale } from '@/api-locale';
import { EveliTasksTable } from '@/eveli-tasks-2';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (
    <Box sx={{ p: 0.5 }}> {/* mock container / wrapper */}
      <EveliTasksTable />
    </Box>)
}
