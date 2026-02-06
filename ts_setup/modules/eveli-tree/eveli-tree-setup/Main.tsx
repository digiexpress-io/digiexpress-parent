import React from 'react';
import { Box } from '@mui/material';
import { EveliTreeTabs } from '../eveli-tree-tabs';

const root = { height: '100%', padding: 1 };

const Main: React.FC<{}> = () => {
  return (
    <Box sx={root}>
      <EveliTreeTabs />
      <Box pt={2}>MAIN</Box>
    </Box>)
}
export { Main }


