import React from 'react';
import { useTheme, CircularProgress, Tab, Tabs, Box } from '@mui/material';

import { FlowReadOnly, FlowReadOnlyGraph } from 'components-hdes/core';


import * as colors from 'components-colors';
import Burger from 'components-burger';
import { Group } from 'descriptor-grouping';


import { useSysConfig } from '../SysConfigContext';

export const FlowYaml: React.FC<{ flowId: string }> = ({ flowId }) => {
  const { hdesSite } = useSysConfig();

  if(!hdesSite) {
    return null;
  }

  const flow = Object.values(hdesSite.flows).find(flow => flow.ast?.name === flowId);
  if(!flow) {
    return <>not available</>;
  }

  return (<FlowReadOnly flow={flow} />);
}

export const FlowGraph: React.FC<{ flowId: string }> = ({ flowId }) => {
  const { hdesSite } = useSysConfig();

  if(!hdesSite) {
    return null;
  }

  const flow = Object.values(hdesSite.flows).find(flow => flow.ast?.name === flowId);
  if(!flow) {
    return <>not available</>;
  }
  const {ast} = flow;
  if(!ast) {
    return <>not available</>;
  }
  return (<FlowReadOnlyGraph flow={ast} site={hdesSite}/>);
}


const TabContent: React.FC<{ selected: number, id: number, children: React.ReactNode }> = ({ selected, id, children }) => {

  if(selected !== id) {
    return null;
  }

  return <>{children}</>;
}

export const SysConfigFlow: React.FC<{ flowId: string }> = ({ flowId }) => {
  const theme = useTheme();
  const { hdesSite } = useSysConfig();
  const [tabValue, setTabValue] = React.useState(0);

  function handleTabValue (event: React.SyntheticEvent, newValue: number) {
    setTabValue(newValue);
  }


  if(!hdesSite) {
    return (<CircularProgress color='secondary'/>);
  }
  
  return (
    <Box sx={{ flexGrow: 1, bgcolor: 'background.paper', display: 'flex' }}>
    <Tabs
        orientation="vertical"
        variant="scrollable"
        value={tabValue}
        onChange={handleTabValue}
        sx={{ borderRight: 1, borderColor: 'divider' }}
      >
        <Tab label="Meta" />
        <Tab label="Flow Desc" />
        <Tab label="Flow Graph" />
      </Tabs>

      <TabContent selected={tabValue} id={0}>
        meta
      </TabContent>

      <TabContent selected={tabValue} id={1}>
        <FlowYaml flowId={flowId} />
      </TabContent>

      <TabContent selected={tabValue} id={2}>
        <FlowGraph flowId={flowId} />
      </TabContent>
    </Box>
  );
}
