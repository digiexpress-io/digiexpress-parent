import React from 'react';
import { Box, Typography, CircularProgress } from '@mui/material';
import { DialobForm, DialobSession } from 'client';
import { TenantEntryDescriptor } from 'descriptor-tenant';


const SessionItem: React.FC<{
  entry: TenantEntryDescriptor,
  form: DialobForm | undefined,
  session: DialobSession | undefined,
}> = ({ form, session }) => {


  if (!form || !session) {
    return (
      <CircularProgress />
    );
  }

  return (
    <Box display='flex' width='100%' alignItems='center'>
      <Box width='10%'><Typography fontWeight='bolder' noWrap>{session.metadata.status}</Typography></Box>
      <Box width='20%' display='flex' alignItems='center'>
        <Typography>{session.metadata.created.toString()}</Typography>
      </Box>
      <Box width='20%' display='flex' alignItems='center'>
        <Typography>{session.metadata.lastAnswer.toLocaleString()}</Typography>
      </Box>
      <Box width='20%'><Typography fontWeight='bolder' noWrap>{session.metadata.owner}</Typography></Box>
      <Box width='20%' display='flex' alignItems='center'>
        <Typography>{session.id}</Typography>
      </Box>
    </Box>
  );
}

export { SessionItem };

