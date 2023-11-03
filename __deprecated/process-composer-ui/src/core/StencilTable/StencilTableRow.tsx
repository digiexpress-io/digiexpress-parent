import React from 'react';
import { TableCell, TableRow, TableCellProps, styled } from '@mui/material';

import { StencilClient } from '@the-stencil-io/composer';
import DeClient from '@declient';

import * as Cells from './StencilTableCells';



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
  row: StencilClient.Article
  tree: DeClient.StencilTree
}> = ({ row, tree }) => {

  return (<TableRow hover tabIndex={-1} key={row.id}>
    <StyledTableCell><Cells.ArticleName      width="300px" row={row} tree={tree}/></StyledTableCell>
  </TableRow>);
}

export default DialobTableRow;

