import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale, EveliApp } from '@/burger'
import { DialobAdminView } from '../frontdesk/views/forms/DialobAdminView';
import { Secondary } from '../frontdesk/Secondary';
import { Toolbar } from '../frontdesk/Toolbar';
import { Box } from '@mui/system';

export const Route = createFileRoute('/secured/$locale/assets/forms/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  
  const { setLocale } = useLocale();
  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} />)

}

const Main: React.FC<{}> = () => {
  return (<Box sx={{ p: 1 }}><DialobAdminView /></Box>)
}