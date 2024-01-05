import React from 'react';
import { Box, Typography, Grid, Divider } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { NavigationSticky } from 'components-generic';

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


const ExtendedNavigationSticky: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  return (
    <NavigationSticky extendedAppBar={(
      <>
        <Divider />
        <DialobItemHeaders />
      </>)}>
      {children}
    </NavigationSticky>
  );
}

export default ExtendedNavigationSticky;


