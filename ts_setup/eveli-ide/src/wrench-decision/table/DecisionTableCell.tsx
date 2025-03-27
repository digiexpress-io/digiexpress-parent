import React from 'react';

import { Box, TableCell, Typography, useTheme, lighten, alpha, darken, SxProps, Tooltip } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';

import { HdesApi } from '@/api-wrench';



function getOutTextFormatting(cell: HdesApi.TypeDef): SxProps {
  if(cell.valueType !== 'STRING') {
    return {};
  }

  return {
    overflow: 'hidden',
    whiteSpace: 'nowrap',
    maxWidth: '200px'
  }
}

const DecisionTableCell: React.FC<{
  row:HdesApi.AstDecisionRow,
  header:HdesApi.TypeDef,
  cell:HdesApi.AstDecisionCell,
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
    sx={{ 
      cursor: "pointer", 
      borderRight: `1px ${borderColor} solid`,
       ...getOutTextFormatting(header)
    }}>
    
    <Typography noWrap>
      { cell?.value ? cell?.value : edit }
    </Typography>

  </TableCell>);
}
export { DecisionTableCell };
