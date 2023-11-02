import React from 'react';
import { Box } from '@mui/material';
import { SxProps } from '@mui/system';

import Burger from '@the-wrench-io/react-burger';
import Context from 'context';
import Core from 'projectcomponents';
import Activities from '../Activities';

const root: SxProps = { height: '100%', backgroundColor: "mainContent.main", width: '100%' };


const Main: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const { site, session } = Context.useComposer();
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
    } else if (active.id === 'projects') {
      return (<Box sx={root}><Core.ProjectsSearch /></Box>);
    }


    if (entity) {
      return <Box sx={root}>no view implemented entity editor: {JSON.stringify(active)}</Box>
    }
    throw new Error("unknown view: " + JSON.stringify(active, null, 2));

  }, [active, site, entity]);
}
export { Main }


