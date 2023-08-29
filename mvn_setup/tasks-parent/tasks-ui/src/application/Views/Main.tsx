import React from 'react';
import { Box } from '@mui/material';
import { SxProps } from '@mui/system';

import Burger from '@the-wrench-io/react-burger';
import TaskClient from '@taskclient';

import Core from '../../core';
import Activities from '../Activities';

const root: SxProps = { height: '100%', backgroundColor: "mainContent.main", width: '100%', p: 1 };


const Main: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const { site, session } = TaskClient.useComposer();
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

    <Box>NEW BOX</Box>


    if (active.id === 'activities') {
      return (<Box sx={root}><Activities /></Box>);
    } else if (active.id === 'tasks') {
      return (<Box sx={root}><Core.Tasks /></Box>);
    } else if (active.id === 'mytasks') {
      return (<Box sx={root}><Core.MyWork /></Box>);
    } else if (active.id === 'dev') {
      return (<Box sx={root}><Core.Dev /></Box>);
    } else if (active.id === 'teamSpace') {
      return (<Box sx={root}><Core.TeamSpace /></Box>)
    } else if (active.id === 'inbox') {
      return (<Box sx={root}><Core.Inbox /></Box>)
    } else if (active.id === 'myoverview') {
      return (<Box sx={root}><Core.MyOverview /></Box>)
    }


    if (entity) {
      return <Box sx={root}>no view implemented entity editor: {JSON.stringify(active)}</Box>
    }
    throw new Error("unknown view: " + JSON.stringify(active, null, 2));

  }, [active, site, entity]);
}
export { Main }


