import React from 'react';
import { TableCell, TableRow } from '@mui/material';


const DecisionTableRow: React.FC<{
  row: any;
  headers: any[];
  renderCell: (props: { row: any; header: any; cell: any; }) => React.ReactNode;
}> = ({ row, headers, renderCell }) => {

  const cells: Record<string, any> = {};
  row.cells.forEach((e: any) => cells[e.header] = e);

  return (
    <TableRow hover role="checkbox" tabIndex={-1}>
      <TableCell align="left" sx={{
        position: 'sticky',
        left: 0,
        backgroundColor: 'secondary.contrastText',
        color: 'primary.contrastText',
        borderBottom: 'unset',
      }}>
        {row.order}
      </TableCell>
      {headers.map((header: any) => (
        <React.Fragment key={header.id}>
          {renderCell({ header, row, cell: cells[header.id] })}
        </React.Fragment>
      ))}
      <TableCell key="_actions" align="center" sx={{ width: 40, padding: 0 }} />
    </TableRow>
  );
};

export { DecisionTableRow };
