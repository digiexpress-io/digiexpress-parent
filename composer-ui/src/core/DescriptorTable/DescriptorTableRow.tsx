import React from 'react';

import { TableCell, TableRow, TableCellProps, styled } from '@mui/material';


import DeClient from '@declient';


const StyledTableCell = styled(TableCell)<TableCellProps>(({ theme }) => ({
  textAlign: 'left',
  fontSize: "13px",
  fontWeight: '400',
  lineHeight: '20px',
  paddingLeft: theme.spacing(2),
  paddingRight: theme.spacing(2),
  paddingTop: theme.spacing(0.5),
  paddingBottom: theme.spacing(0.5),
}));



const DescriptorTableRow: React.FC<{
  row: DeClient.ServiceDescriptor,
  assocs: DeClient.DefStateAssocs,
  def: DeClient.DefinitionState
}> = ({ row, def, assocs }) => {

  const stencil = def.definition.refs.find(ref => ref.type === 'STENCIL')?.tagName;
  const hdes = def.definition.refs.find(ref => ref.type === 'HDES')?.tagName;

  const dialob = assocs.getDialob(row.formId);
  const formName = dialob ? `${dialob.rev.name} / ${dialob.entry.revisionName}` : '-';

  const flow = assocs.getFlow(row.flowId);
  const flowName = flow ? `${flow.flow?.ast?.name} / ${hdes}` : '-'

  return (<TableRow hover tabIndex={-1} key={row.id}>
    <StyledTableCell>{row.name}</StyledTableCell>
    <StyledTableCell>{formName}</StyledTableCell>
    <StyledTableCell>{flowName}</StyledTableCell>
    <StyledTableCell>{}</StyledTableCell>
  </TableRow>);
}

export default DescriptorTableRow;

