import { Divider } from '@mui/material';
import { Box } from '@mui/system';

import React, { PropsWithChildren } from 'react';

const titleStyle = {
  display: 'flex',
};
const dividerStyle = {
  mt: 1,
  mb: 2,
};

export const SectionTitle: React.FC<PropsWithChildren> = ({ children }) => {

  return (
    <>
      <div>
        <Box sx={ titleStyle } >
          {children}  
        </Box>    
      </div>
      <Divider sx={ dividerStyle } />
    </>
  );
}
