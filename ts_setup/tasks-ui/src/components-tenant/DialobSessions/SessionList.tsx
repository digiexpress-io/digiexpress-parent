import React from 'react';
import { Stack, Grid, Typography, TablePagination, Alert, Box, AppBar, Toolbar } from '@mui/material';

import { FormattedMessage } from 'react-intl';
import Pagination from 'table';
import { initTable } from './types';
import { StyledStackItem } from './SessionListStyles';
import { TenantEntryDescriptor } from 'descriptor-tenant';

import { SessionItem } from './SessionItem';
import { DialobForm, DialobSession } from 'client';




const SessionHeaders: React.FC<{}> = () => {
  return (
    <AppBar color='inherit' position='sticky' sx={{ boxShadow: 1 }}>
      <Toolbar sx={{ backgroundColor: 'table.main', '&.MuiToolbar-root': { p: 1, m: 0 } }}>
        <Stack direction='row' spacing={1} alignItems='center'>
          <Grid container>
            <Grid item md={8} lg={8} xl={8} sx={{ px: 2, py: 1 }}>
              <Box display='flex' >
                <Box width='38%'><Typography fontWeight='bold'><FormattedMessage id='dialob.form.title' /></Typography></Box>
                <Box width='38%'><Typography fontWeight='bold'><FormattedMessage id='dialob.form.technicalName' /></Typography></Box>
                <Box width='12%' display='flex' alignItems='center'>
                  <Typography fontWeight='bold'><FormattedMessage id='dialob.form.created' /></Typography>
                </Box>
                <Box width='12%' display='flex' alignItems='center'>
                  <Typography fontWeight='bold'><FormattedMessage id='dialob.form.lastSaved' /></Typography>
                </Box>
              </Box>
            </Grid>
            <Grid item md={4} lg={4} xl={4} sx={{ px: 2, py: 1 }} />
          </Grid>
        </Stack>
      </Toolbar>
    </AppBar>
  )
}


const RowFiller: React.FC<{ value: Pagination.TablePagination<DialobSession> }> = ({ value }) => {

  if (value.entries.length === 0) {
    return (<Alert sx={{ m: 2 }} severity='info'>
      <Typography><FormattedMessage id='core.myWork.alert.entries.none' /></Typography>
    </Alert>);
  }
  return <></>
}

const SessionList: React.FC<{
  form: DialobForm,
  sessions: DialobSession[];
  entry: TenantEntryDescriptor;
}> = ({ sessions, form, entry }) => {
  const [table, setTable] = React.useState(initTable([]).withSrc(sessions).withPage(0));

  function handleOnPageChange(_garbageEvent: any, newPage: number) {
    setTable((state) => state.withPage(newPage));
  }

  function handleOnRowsPerPageChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTable((state) => state.withRowsPerPage(parseInt(event.target.value, 10)))
  }

  return (<Grid container>
    <SessionHeaders />

    <Grid item md={8} lg={8}>
      <Stack sx={{ backgroundColor: 'mainContent.main' }}>
        {table.entries.map((session, index) => (
          <StyledStackItem key={session.id} index={index} onClick={() => { }}>
            <SessionItem key={session.id} entry={entry} form={form} session={session} />
          </StyledStackItem>)
        )}
        <RowFiller value={table} />
      </Stack>

      <TablePagination
        rowsPerPageOptions={table.rowsPerPageOptions}
        component="div"
        count={table.src.length}
        rowsPerPage={table.rowsPerPage}
        page={table.page}
        onPageChange={handleOnPageChange}
        onRowsPerPageChange={handleOnRowsPerPageChange}
      />
    </Grid>
  </Grid >
  );
}

export { SessionList };



