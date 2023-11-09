import React from 'react';
import { Box, Typography } from '@mui/material';
import { TenantEntryDescriptor } from 'descriptor-tenant';
import Burger from 'components-burger';


const DialobItem: React.FC<{ entry: TenantEntryDescriptor }> = ({ entry }) => {

  return (
    <Box display='flex' width='100%' alignItems='center'>
      <Box width='38%'><Typography fontWeight='bolder' noWrap>{entry.formTitle}</Typography></Box>
      <Box width='38%'><Typography fontWeight='bolder' noWrap>{entry.formName}</Typography></Box>
      <Box width='12%' display='flex' alignItems='center'>
        <Typography><Burger.DateTimeFormatter type='date' value={entry.created} /></Typography>
      </Box>
      <Box width='12%' display='flex' alignItems='center'>
        <Typography><Burger.DateTimeFormatter type='date' value={entry.lastSaved} /></Typography>
      </Box>
    </Box>
  );
}

export { DialobItem };

