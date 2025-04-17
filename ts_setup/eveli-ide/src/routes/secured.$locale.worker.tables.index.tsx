import React from 'react'
import { Box } from '@mui/material';
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/api-locale';
import { EveliTableWithData } from '@/eveli-table/EveliTableWithData';
import { TableTester } from '@/eveli-table/TableTester';

export const Route = createFileRoute('/secured/$locale/worker/tables/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (
    <Box sx={{ p: 0.5 }}> {/* mock container / wrapper */}
      <TableTester />
    </Box>)
}
