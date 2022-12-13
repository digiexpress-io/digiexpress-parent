import React from 'react';
import { Box } from '@mui/material';
import { SxProps } from '@mui/system';

import Burger from '@the-wrench-io/react-burger';

import Activities from './Activities';

import ComposerServices from './composer-services';
import DeClient from './DeClient';



const root: SxProps = { height: `100%`, backgroundColor: "mainContent.main" };


const Main: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const { site, session } = DeClient.useComposer();
  const tabs = layout.session.tabs;
  const active = tabs.length ? tabs[layout.session.history.open] : undefined;
  const entity = active ? session.getEntity(active.id) : undefined;
  console.log("Opening Route", active?.id);
      
  //composers which are NOT linked directly with an article

  return React.useMemo(() => {
    if (site.contentType === "NO_CONNECTION") {
      return (<Box>{site.contentType}</Box>);
    }
    if (!active) {
      return null;
    }
    if (active.id === 'activities') {
      return (<Box sx={root}><Activities /></Box>);
    } 
    
    if(entity && entity.kind === 'DEF') {
      const value: DeClient.ServiceDefinition = entity.delegate;
      return <Box sx={root}><ComposerServices value={value}/></Box>      
    }
    
    if (entity) {
      return <Box sx={root}>entity editor: {JSON.stringify(active)}</Box>
    }
    throw new Error("unknown view: " + JSON.stringify(active, null, 2));

  }, [active, site, entity]);
}
export { Main }


