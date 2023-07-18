import React from 'react';

import { Box, TableCell, TableRow, Typography } from '@mui/material';

import { Client } from '../../context';

interface RowEditMode {
  rule?: { cell: Client.AstDecisionCell },
  header?: { header: Client.TypeDef },
}

const RowEdit: React.FC<{
  row: Client.AstDecisionRow,
  headers: Client.TypeDef[],
  onChange: (edit: RowEditMode) => void
}> = ({ row, headers, onChange }) => {

  const cells: Record<string, Client.AstDecisionCell> = {};
  row.cells.forEach(e => cells[e.header] = e);

  return (<TableRow hover role="checkbox" tabIndex={-1} key={row.id}>
  
    <TableCell align="left" sx={{
      position: "sticky",
      left: 0,
      backgroundColor: "page.main",
      color: "primary.contrastText",
      borderBottom: "unset"
    }}>
      {row.order}
    </TableCell>

    {headers.map(header => {
      const cell = cells[header.id];
      if (header.direction === "IN") {
        return (<TableCell key={cell.header} onClick={() => onChange({ rule: { cell } })} sx={{cursor: "pointer"}}>
          <Typography noWrap>
            {cell?.value ? cell.value : <Box sx={{ fontWeight: "bold" }} component="span">*</Box>}
          </Typography>
        </TableCell>);
      }

      return (<TableCell key={cell.header} onClick={() => onChange({ rule: { cell } })} sx={{cursor: "pointer"}}>
        <Typography noWrap>
          {cell?.value}
        </Typography>
      </TableCell>);
    })}
  </TableRow>);
}
export type { RowEditMode };
export { RowEdit };
