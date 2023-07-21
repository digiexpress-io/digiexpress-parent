import React from 'react';
import { Box, Paper } from '@mui/material';



const Sticky: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (<Box sx={{ width: "100%" }}>
    <Box sx={{ position: 'fixed',  width: "70%", m: 1, display: "flex", flexDirection: "row" }} >
      {children}
    </Box>
  </Box>);
}

export default Sticky;
