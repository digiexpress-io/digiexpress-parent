import React from 'react';
import { TableCell, TableRow, TableCellProps, styled } from '@mui/material';

import DeClient from '@declient';

import * as Cells from './HdesTableCells';



const StyledTableCell = styled(TableCell)<TableCellProps>(({ theme }) => ({
  textAlign: 'left',
  fontSize: "13px",
  fontWeight: '400',
  lineHeight: '28px',
  
  paddingLeft: theme.spacing(2),
  paddingRight: theme.spacing(2),
  paddingTop: theme.spacing(0),
  paddingBottom: theme.spacing(0),
}));



const DialobTableRow: React.FC<{
  tree: DeClient.HdesTree,
  row: DeClient.HdesBodyEntity
}> = ({ row, tree }) => {

  return (<TableRow hover tabIndex={-1} key={row.id}>
    <StyledTableCell><Cells.AssetName      width="300px" row={row} tree={tree}/></StyledTableCell>
    <StyledTableCell><Cells.AssetType      width="300px" row={row} tree={tree}/></StyledTableCell>
  </TableRow>);
}

export default DialobTableRow;

