import React from 'react';
import { TableCell, TableRow, TableCellProps, styled } from '@mui/material';

import DeClient from '@declient';

import * as Cells from './DescriptorTableCells';


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



const DescriptorTableRow: React.FC<{
  row: DeClient.ServiceDescriptor,
  assocs: DeClient.DefStateAssocs,
  def: DeClient.DefinitionState,
}> = ({ row, def, assocs }) => {

  return (<TableRow hover tabIndex={-1} key={row.id}>
    <StyledTableCell><Cells.DescriptorName  width="200px" row={row} assocs={assocs} def={def}/></StyledTableCell>
    <StyledTableCell><Cells.DialobName      width="300px" row={row} assocs={assocs} def={def}/></StyledTableCell>
    <StyledTableCell><Cells.FlowName        width="300px" row={row} assocs={assocs} def={def}/></StyledTableCell>
    <StyledTableCell><Cells.WorkflowName    width="150px" row={row} assocs={assocs} def={def}/></StyledTableCell>
  </TableRow>);
}

export default DescriptorTableRow;

