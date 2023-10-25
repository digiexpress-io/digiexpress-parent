import React from 'react';
import { Stack, Toolbar, AppBar } from '@mui/material';


const NavigationSticky: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  return (
    <AppBar color='inherit' position='sticky' sx={{ boxShadow: 1 }}>
      <Toolbar sx={{ backgroundColor: 'table.main', '&.MuiToolbar-root': { p: 1, m: 0 } }}>
        <Stack direction='row' spacing={1} alignItems='center'>
          {children}
        </Stack>
      </Toolbar>
    </AppBar>
  );
}

export default NavigationSticky;