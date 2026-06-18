import React from 'react';
import { TableCell, TableRow, IconButton } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';
import { Delete as DeleteIcon } from '@mui/icons-material';

const DecisionTableRow: React.FC<{
  row: Fs.DecisionAstRow;
  headers: Fs.TypeDef[];
  dragProps?: React.HTMLAttributes<HTMLTableRowElement>;
  onDelete: (id: string) => void;
  renderCell: (props: { row: Fs.DecisionAstRow; header: Fs.TypeDef; cell: Fs.DecisionAstCell; }) => React.ReactNode;
}> = ({ row, headers, renderCell, dragProps, onDelete }) => {

  const cells: Record<string, Fs.DecisionAstCell> = {};
  row.cells.forEach((e) => cells[e.header] = e);

  return (
    <TableRow hover role="checkbox" tabIndex={-1} key={row.id} {...dragProps}>
      <TableCell key='_reserved' align="left" sx={{
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
      <TableCell key="_actions" align="center" sx={{ width: 40, padding: 0 }}>
        <IconButton
          size="small"
          color="error"
          onClick={(e) => {
            e.stopPropagation();
            onDelete(row.id);
          }}
        >
          <DeleteIcon fontSize="small" />
        </IconButton>
      </TableCell>
    </TableRow>
  );
}

export { DecisionTableRow };
