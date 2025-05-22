import React from 'react';

import { TableCell, TableRow } from '@mui/material';

import { HdesApi } from '@/api-wrench';

import DeleteIcon from '@mui/icons-material/Delete';
import IconButton from '@mui/material/IconButton';



const DecisionTableRow: React.FC<{
  row: HdesApi.AstDecisionRow,
  headers: HdesApi.TypeDef[],
  renderCell: (props: {
    row: HdesApi.AstDecisionRow;
    header: HdesApi.TypeDef;
    cell: HdesApi.AstDecisionCell;
  }) => React.ReactNode;
  dragProps?: React.HTMLAttributes<HTMLTableRowElement>;
  onDelete: (id: string) => void;
}> = ({ row, headers, renderCell, dragProps, onDelete }) => {

  const cells: Record<string, HdesApi.AstDecisionCell> = {};
  row.cells.forEach(e => cells[e.header] = e);

  return (
    <TableRow hover role="checkbox" tabIndex={-1} key={row.id} {...dragProps}>
      <TableCell key='_reserved' align="left" sx={{
        position: "sticky",
        left: 0,
        backgroundColor: "secondary.contrastText",
        color: "primary.contrastText",
        borderBottom: "unset"
      }}>
        {row.order}
      </TableCell>
      {headers.map(header => (<React.Fragment key={header.id}>{renderCell({ header, row, cell: cells[header.id] })}</React.Fragment>))}
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
    </TableRow>);
}
export { DecisionTableRow };
