import React from 'react';
import { Box } from '@mui/material';
import { FsTabs } from '../fs-tabs';
import { FsBreadcrumb } from '../fs-breadcrumb';
import { FsMain } from '../fs-main';
import { useFsNav } from '@dxs-ts/fs-api';
import { FsColors } from '../fs-theme';

const Main: React.FC<{}> = () => {
  const { isDarkMode } = useFsNav();

  return (
    <Box sx={{
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.background,
      color: isDarkMode ? FsColors.dark.text : FsColors.light.text
    }}>
      <FsTabs />
      <FsBreadcrumb />
      <Box sx={{ flex: 1, overflow: 'hidden' }}>
        <FsMain />
      </Box>
    </Box>)
}
export { Main }


