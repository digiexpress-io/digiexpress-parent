import React from 'react';

import { TableCell, TableRow } from '@mui/material';

import { Client } from '../../context';


const DecisionTableRow: React.FC<{
  row: Client.AstDecisionRow,
  headers: Client.TypeDef[],
  renderCell: (props: {
    row: Client.AstDecisionRow;
    header: Client.TypeDef;
    cell: Client.AstDecisionCell;
  }) => React.ReactNode
}> = ({ row, headers, renderCell }) => {

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
    {headers.map(header => renderCell({ header, row, cell: cells[header.id]}))}
  </TableRow>);
}
export { DecisionTableRow };
