import React from 'react';
import { Box } from '@mui/material';
import { FsTabs } from '../fs-tabs';
import { FsMain } from '../fs-main';
import { FsColors } from '../fs-theme';


const Main: React.FC<{}> = () => {

  return (
    <Box sx={{
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      backgroundColor: FsColors.light.background,
      color: FsColors.light.text
    }}>
      <FsTabs />
      <Box sx={{ flex: 1, overflow: 'hidden' }}>
        <FsMain />
      </Box>
    </Box>)
}
export { Main }


