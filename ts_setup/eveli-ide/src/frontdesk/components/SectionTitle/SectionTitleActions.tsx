import React from 'react';
import { Box } from '@mui/system';

const style = {
  display: 'flex',
  flex: '1',
  justifyContent: 'flex-end',
};


export const SectionTitleActions: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  return (
    <div>
      <Box sx={{ ...style }} />
      {children}
    </div>
  );
}
