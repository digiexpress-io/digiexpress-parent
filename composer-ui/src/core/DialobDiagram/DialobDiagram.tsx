import React from "react";
import {
  Box, Stack, CircularProgress
} from "@mui/material";

import {
} from '@mui/icons-material';
import { FormattedMessage, useIntl } from 'react-intl';


import DeClient from '@declient';


import DialobCanvas from './DialobCanvas';

const DialobDiagram: React.FC<{
  dialob: DeClient.DefStateDialobAssocs,
  def: DeClient.DefinitionState,
  canvas: { width: number, height: number}
}> = ({ dialob, def, canvas }) => {

  return (<Box sx={{ height: '100%', width: "100%" }}><DialobCanvas dialob={dialob} def={def} canvas={canvas}/></Box>);
}

export default DialobDiagram;
