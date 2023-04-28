import React from 'react';
import { Paper, useTheme } from '@mui/material';

import DialobDiagram from '../../DialobDiagram';
import DeClient from '@declient';


const CellInfoDialob: React.FC<{
  row: DeClient.ServiceDescriptor,
  assocs: DeClient.DefStateAssocs,
  def: DeClient.DefinitionState,
  id: string
}> = ({ row, def, assocs }) => {
  const theme = useTheme();
  const dialob = assocs.getDialob(row.formId);

  return (<Paper sx={{
    width: "1200px", height: "700px", overflow: "auto",
    backgroundColor: theme.palette.explorer.main
  }}>
    <DialobDiagram def={def} dialob={dialob!} canvas={{ width: 1200, height: 700}}/>
  </Paper>)
};

export default CellInfoDialob;

