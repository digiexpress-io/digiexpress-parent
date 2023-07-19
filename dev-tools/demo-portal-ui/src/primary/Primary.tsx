import React from 'react';
import { Box } from '@mui/material';
import { Content } from './Content';
import { Footer } from './Footer';


const Primary: React.FC<{}> = () => {
  return (
    <Box display="flex" flexDirection="column">
      <Box sx={{ m: 2}}>
        <Content />
      </Box>
      
      <Box flexGrow={1}/>
      
      <Box sx={{ ml: 0, mr: 0 }}>
        <Footer />
      </Box>
    </Box>
  );
}
export { Primary };

