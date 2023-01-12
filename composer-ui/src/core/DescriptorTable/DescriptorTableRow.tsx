import React from 'react';

import {TableCell, TableRow} from '@mui/material';


import DeClient from '@declient';


const DescriptorTableRow: React.FC<{
  row: DeClient.ServiceDescriptor,
  def: DeClient.DefStateAssocs
}> = ({ row, def }) => {

  const dialob = def.getDialob(row.formId);
  const formName = dialob ? `${dialob.rev.name} / ${dialob.entry.revisionName}` : '-';


  return (
    <TableRow hover tabIndex={-1} key={row.id}>
      <TableCell align="left">{row.name}</TableCell>
      <TableCell align="left">{row.desc}</TableCell>
      <TableCell align="left">{formName}</TableCell>
      <TableCell align="left">{row.flowId}</TableCell>
      <TableCell align="left">{row.id}</TableCell>
    </TableRow>
  );
}

export default DescriptorTableRow;

