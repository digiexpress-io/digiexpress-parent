import React from 'react';
import { Box } from '@mui/material';
import { Content } from './Content';


const Primary: React.FC<{}> = () => {
  return (
    <Box sx={{p: 2}}>
      <Content />
    </Box>
  );
}
export { Primary };

