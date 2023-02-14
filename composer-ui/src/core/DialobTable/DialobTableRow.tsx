import React from 'react';
import { TableCell, TableRow, TableCellProps, styled } from '@mui/material';

import DeClient from '@declient';

import * as Cells from './DialobTableCells';


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
  row: DeClient.DialobFormRevisionDocument
  tree: DeClient.DialobTree
}> = ({ row, tree }) => {

  return (<TableRow hover tabIndex={-1} key={row.id}>
    <StyledTableCell><Cells.DialobName      width="300px" row={row} tree={tree}/></StyledTableCell>
  </TableRow>);
}

export default DialobTableRow;

