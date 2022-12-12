import React from 'react';
import { Box, Typography, Divider } from '@mui/material';

import { StoreError } from '../error-types';



const ErrorView: React.FC<{ error: StoreError }> = ({ error }) => {
  
  console.log(error);
  const items: React.ReactNode[] = [];
  for (let index = 0; index < error.errors?.length; index++) {
    const v = error.errors[index];
    
    items.push(<Box key={index}>
      <Box sx={{pb: 1, color: "error.main"}}>
        <Typography variant="h4">{v.id}</Typography>
      </Box>
      <Typography variant="body1" sx={{ fontSize: 11 }}>{v.value}</Typography>
    </Box>);

    if (index < error.errors.length - 1) {
      items.push(<Divider key={index + "-divider"} sx={{mb: 2}}/>);
    }
  }

  return (<Box sx={{ width: '100%', p: 1 }}>{items}</Box>);
}


export default ErrorView;
