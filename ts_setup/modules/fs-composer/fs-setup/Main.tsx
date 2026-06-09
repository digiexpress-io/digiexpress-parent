import React from 'react';
import { useFsTheme } from '../fs-theme';
import { Box } from '@mui/material';
import { FsTabs } from '../fs-tabs';
import { FsMain } from '../fs-main';
import { FsColors } from '../fs-theme';
import { FsuProvider } from '@dxs-ts/fs-api';

const Main: React.FC<{}> = () => {
  const { isDarkMode } = useFsTheme();


  return (
    <Box sx={{
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.background,
      color: isDarkMode ? FsColors.dark.text : FsColors.light.text
    }}>
      <FsTabs />
      <Box sx={{ flex: 1, overflow: 'hidden' }}>
        <FsuProvider>
          <FsMain />
        </FsuProvider>

      </Box>
    </Box>)
}
export { Main }


