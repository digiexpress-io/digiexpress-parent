import React from 'react';
import { Stack, Toolbar, AppBar, Box, Typography, Grid, Divider } from '@mui/material';
import { FormattedMessage } from 'react-intl';


// Box widths must match those defined in DialobItem.tsx to align the columns
const DialobItemHeaders: React.FC<{}> = () => {
  return (<Grid container>
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
  )
}


const NavigationSticky: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  return (
    <AppBar color='inherit' position='sticky' sx={{ boxShadow: 1 }}>
      <Toolbar sx={{ backgroundColor: 'table.main', '&.MuiToolbar-root': { p: 1, m: 0 } }}>
        <Stack direction='row' spacing={1} alignItems='center'>
          {children}
        </Stack>
      </Toolbar>
      <Divider />
      <DialobItemHeaders />
    </AppBar>
  );
}

export default NavigationSticky;