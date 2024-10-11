import React from 'react';

import { Box, TableCell, Typography, useTheme, lighten, alpha, darken } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';

import { HdesApi as Client } from '../../client';


const DecisionTableCell: React.FC<{
  row: Client.AstDecisionRow,
  header: Client.TypeDef,
  cell: Client.AstDecisionCell,
  onClick: () => void
}> = ({ header, cell, onClick }) => {

  const theme = useTheme();
  const borderColor = theme.palette.mode === 'light' ? lighten(alpha(theme.palette.divider, 1), 0.88) : darken(alpha(theme.palette.divider, 1), 0.68);

  const edit = <EditIcon />;

  if (header.direction === "IN") {
    return (<TableCell key={cell.header} onClick={() => onClick()} sx={{ cursor: "pointer", borderRight: `1px ${borderColor} solid` }}>
      <Typography noWrap display="flex">
        {cell?.value ? cell.value : <Box sx={{ fontWeight: "bold" }} component="span">{edit}</Box>}
      </Typography>
    </TableCell>);
  }

  return (<TableCell 
    key={cell.header} 
    onClick={() => onClick()} 
    sx={{ cursor: "pointer", borderRight: `1px ${borderColor} solid` }}>
    
    <Typography noWrap display="flex">
      { cell?.value ? cell?.value : edit }
    </Typography>
    
  </TableCell>);
}
export { DecisionTableCell };
