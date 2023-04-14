import React from 'react';
import { Paper, useTheme } from '@mui/material';


import FlowDiagram from '../../FlowDiagram';
import DeClient from '@declient';



const CellInfoFlow: React.FC<{
  row: DeClient.ServiceDescriptor,
  assocs: DeClient.DefStateAssocs,
  def: DeClient.DefinitionState,
  id: string
}> = ({ row, def, assocs }) => {
  const theme = useTheme();
  const flow = assocs.getFlow(row.flowId)!;

  return (<Paper sx={{
    width: "700px", height: "700px", overflow: "auto",
    backgroundColor: theme.palette.explorer.main
  }}>
    <FlowDiagram def={def} flow={flow} canvas={{ width: 700, height: 700}}/>
  </Paper>)
};

export default CellInfoFlow;

