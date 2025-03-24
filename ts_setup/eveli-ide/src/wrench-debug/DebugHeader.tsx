import React from 'react';

import { Box, TableCell, TableHead, TableRow } from '@mui/material';


const DebugHeader: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const totalCols = 2;
  return (<TableHead sx={{ position: "sticky", top: 0 }}>
    <TableRow>
      <TableCell align="left" colSpan={totalCols} sx={{ fontWeight: "bold", pl: 0 }}>
        <Box display="flex" alignItems="center">
          {children}
        </Box>
      </TableCell>
    </TableRow>

  </TableHead>);
}

export type { };
export { DebugHeader };
