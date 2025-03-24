import React from 'react';

import { TableCell, TableRow } from '@mui/material';

import { HdesApi } from '@/api-wrench';


const DecisionTableRow: React.FC<{
  row:HdesApi.AstDecisionRow,
  headers:HdesApi.TypeDef[],
  renderCell: (props: {
    row:HdesApi.AstDecisionRow;
    header:HdesApi.TypeDef;
    cell:HdesApi.AstDecisionCell;
  }) => React.ReactNode
}> = ({ row, headers, renderCell }) => {

  const cells: Record<string,HdesApi.AstDecisionCell> = {};
  row.cells.forEach(e => cells[e.header] = e);

  return (<TableRow hover role="checkbox" tabIndex={-1} key={row.id}>
    <TableCell align="left" sx={{
      position: "sticky",
      left: 0,
      backgroundColor: "secondary.contrastText",
      color: "primary.contrastText",
      borderBottom: "unset"
    }}>
      {row.order}
    </TableCell>
    {headers.map(header => renderCell({ header, row, cell: cells[header.id]}))}
  </TableRow>);
}
export { DecisionTableRow };
