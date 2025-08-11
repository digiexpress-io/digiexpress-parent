import React from 'react'
import { Outlet, createFileRoute } from '@tanstack/react-router'
import { Box } from '@mui/material';

import { Secondary } from '../eveli-setup/Secondary';
import { Toolbar } from '../eveli-setup/Toolbar';
import { EveliApp } from '../eveli-app';


export const Route = createFileRoute('/secured/$locale/worker')({
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