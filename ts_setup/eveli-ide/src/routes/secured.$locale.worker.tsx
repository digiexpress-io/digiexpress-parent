import React from 'react'
import { Outlet } from '@tanstack/react-router'

import { Secondary } from '../eveli-setup/Secondary';
import { Toolbar } from '../eveli-setup/Toolbar';
import { Box } from '@mui/material';
import { EveliApp } from '@/eveli-app';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  return (<EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} />)
}


const Main: React.FC<{}> = () => {

  return (<Box p={1}>
    <Outlet />
  </Box>)
}