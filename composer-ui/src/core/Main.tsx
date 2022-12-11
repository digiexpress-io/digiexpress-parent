import React from 'react';
import { Box } from '@mui/material';
import { SxProps } from '@mui/system';

import Burger from '@the-wrench-io/react-burger';

import Activities from './Activities';
import { Composer } from './context';

import ComposerServices from './composer-services';
import { Client } from './context';



const root: SxProps = { height: `100%`, backgroundColor: "mainContent.main" };


const Main: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const { site, session } = Composer.useComposer();
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
    
    if(entity && entity.type === 'SERVICE_DEF') {
      return <Box sx={root}><ComposerServices value={entity as Client.ServiceDefinitionDocument}/></Box>      
    }
    
    if (entity) {
      return <Box sx={root}>entity editor: {JSON.stringify(active)}</Box>
    }
    throw new Error("unknown view: " + JSON.stringify(active, null, 2));

  }, [active, site, entity]);
}
export { Main }


