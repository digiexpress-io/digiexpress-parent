import React from 'react';
import { TableCell, TableRow } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';


const DecisionTableRow: React.FC<{
  row: Fs.DecisionAstRow;
  headers: Fs.DecisionTypeDef[];
  renderCell: (props: { row: Fs.DecisionAstRow; header: Fs.DecisionTypeDef; cell: Fs.DecisionAstCell | undefined; }) => React.ReactNode;
}> = ({ row, headers, renderCell }) => {

  const cells: Record<string, Fs.DecisionAstCell> = {};
  row.cells.forEach((e) => cells[e.header] = e);

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
      {headers.map((header) => (
        <React.Fragment key={header.id}>
          {renderCell({ header, row, cell: cells[header.id] })}
        </React.Fragment>
      ))}
      <TableCell key="_actions" align="center" sx={{ width: 40, padding: 0 }} />
    </TableRow>
  );
};

export { DecisionTableRow };
