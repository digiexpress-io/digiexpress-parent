import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/api-locale';
import { EveliTableWithData } from '@/eveli-table/EveliTableWithData';
import { TableTester } from '@/eveli-table/TableTester';
import { Box } from '@mui/system';

export const Route = createFileRoute('/secured/$locale/worker/tables/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (
    <Box sx={{ p: 2 }}> {/* mock container / wrapper */}
      <TableTester />
    </Box>)
}
