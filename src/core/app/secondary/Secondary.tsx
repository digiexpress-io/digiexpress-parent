import React from 'react';
import { Box } from '@mui/material';

import { Topics } from './Topics';


const Secondary: React.FC<{}> = () => {
  return (<Box sx={{ backgroundColor: 'primary.main', color: 'primary.contrastText', height: "100%"}}>
    <Topics />
  </Box>);
}


export { Secondary };

