import React from 'react';
import { Stack, Grid, Typography, TablePagination, Alert, Box, AppBar, Toolbar, Divider } from '@mui/material';

import { FormattedMessage } from 'react-intl';
import Pagination from 'table';
import { initTable } from './types';
import { StyledStackItem } from './SessionListStyles';
import { TenantEntryDescriptor } from 'descriptor-dialob';

import { SessionItem } from './SessionItem';
import { DialobForm, DialobSession } from 'client';
import Burger from 'components-burger';
import { wash_me } from 'components-colors';

const SessionHeaders: React.FC<{ entry: TenantEntryDescriptor }> = ({ entry }) => {
  return (
    <AppBar color='inherit' position='sticky' sx={{ boxShadow: 1 }}>
      <Toolbar sx={{ backgroundColor: 'table.main', '&.MuiToolbar-root': { p: 0, m: 0 } }}>
        <Stack direction='row' spacing={1} alignItems='center' width={1}>
          <Grid container>

            <Grid item md={3} lg={3} xl={3} sx={{ pl: 1, pt: 1 }}>
              <Typography fontWeight='bold'><FormattedMessage id='dialob.form.title' /></Typography>
            </Grid>
            <Grid item md={9} lg={9} xl={9} sx={{ pl: 1, pt: 1 }}>
              <Typography>{entry.formTitle}</Typography>
            </Grid>

            <Grid item md={3} lg={3} xl={3} sx={{ pl: 1 }}>
              <Typography fontWeight='bold'><FormattedMessage id='dialob.form.technicalName' /></Typography>
            </Grid>
            <Grid item md={9} lg={9} xl={9} sx={{ pl: 1 }}>
              <Typography>{entry.formName}</Typography>
            </Grid>

            <Grid item md={3} lg={3} xl={3} sx={{ pl: 1 }}>
              <Typography fontWeight='bold'><FormattedMessage id='dialob.form.lastSaved' /></Typography>
            </Grid>
            <Grid item md={9} lg={9} xl={9} sx={{ pl: 1 }}>
              <Typography><Burger.DateTimeFormatter type='dateTime' value={entry.lastSaved} /></Typography>
            </Grid>

            <Grid item md={12} lg={12} xl={12} sx={{ py: 1 }}>
              <Divider />
            </Grid>

            <Grid item md={12} lg={12} xl={12} sx={{ pl: 1 }}>
              <Typography fontWeight='bold'><FormattedMessage id='dialob.form.sessions' /></Typography>
            </Grid>
            <Grid item md={12} lg={12} xl={12} sx={{ p: 1 }}>
              <Box display='flex' >
                <Box width='15%'><Typography fontWeight='bold'><FormattedMessage id='dialob.form.sessions.table.status' /></Typography></Box>
                <Box width='15%'><Typography fontWeight='bold'><FormattedMessage id='dialob.form.sessions.table.created' /></Typography></Box>
                <Box width='15%'><Typography fontWeight='bold'><FormattedMessage id='dialob.form.sessions.table.lastAnswered' /></Typography></Box>
                <Box width='25%'><Typography fontWeight='bold'><FormattedMessage id='dialob.form.sessions.table.owner' /></Typography></Box>
                <Box width='25%'><Typography fontWeight='bold'><FormattedMessage id='dialob.form.sessions.table.sessionId' /></Typography></Box>
                <Box width='5%' />
              </Box>
            </Grid>

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
    <SessionHeaders entry={entry} />

    <Grid item md={12} lg={12} width='100%'>
      <Stack sx={{ backgroundColor: wash_me }}>
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
