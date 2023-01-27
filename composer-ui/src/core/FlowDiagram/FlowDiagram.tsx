import React from "react";
import {
  Box, Stack, CircularProgress
} from "@mui/material";

import {
} from '@mui/icons-material';
import { FormattedMessage, useIntl } from 'react-intl';


import DeClient from '@declient';


import FlowCanvas from './FlowCanvas';

/**

    <Box sx={{ backgroundColor: 'mainContent.main' }}>
      <ComposerMenu value={value}/>
      <Box sx={{ width: '40%', ml: 2, mr: 2 }}>
        <Stack spacing={2}>
          <Box></Box>
          {value.processes.map((proc, key) => <ProcessCard key={key} value={proc} />)}
        </Stack>
      </Box>
    </Box>

 */

const FlowDiagram: React.FC<{
  flow: DeClient.DefStateFlowAssocs,
  def: DeClient.DefinitionState,
  canvas: { width: number, height: number}
}> = ({ flow, def, canvas }) => {

  return (<Box sx={{ height: '100%', width: "100%" }}><FlowCanvas flow={flow} def={def} canvas={canvas}/></Box>);
}

export default FlowDiagram;
