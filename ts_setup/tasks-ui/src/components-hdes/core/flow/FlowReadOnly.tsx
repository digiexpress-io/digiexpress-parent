import React from 'react';
import { Box } from '@mui/material';

import CodeEditor from '../../code-editor';
import { Client } from '../context';
import Graph from './graph';

export const FlowReadOnlyGraph: React.FC<{ flow: Client.AstFlow, site: Client.Site }> = ({ flow, site }) => {
  
  return (<Box sx={{ top: "64px", right: "30px" }}>
    <Graph flow={flow} site={site}
      onClick={() => console.log("single")}
      onDoubleClick={(id) => {
      }} />
  </Box>);
}


export const FlowReadOnly: React.FC<{ flow: Client.Entity<Client.AstFlow> }> = ({ flow }) => {
  const src = flow.ast?.src.value;  
  return (<Box height="100%">
    <CodeEditor id={flow.id} mode="yaml" src={src ? src : "#--failed-to-parse"} />
  </Box>);
}

