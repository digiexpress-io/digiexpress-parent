import React from 'react';
import { Box } from '@mui/material';
import { SxProps } from '@mui/system';

import Burger from 'components-burger';
import Context from 'context';
import { DialobList } from 'components-dialob';
import Activities from '../Activities';
import { wash_me } from 'components-colors';

const root: SxProps = { height: '100%', backgroundColor: wash_me, width: '100%' };


const Main: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const { site, session } = Context.useComposer();
  const tabs = layout.session.tabs;
  const active = tabs.length ? tabs[layout.session.history.open] : undefined;
  const entity = active ? session.getEntity(active.id) : undefined;
  console.log("Opening Route", active?.id);

  //composers which are NOT linked directly with an article

  return React.useMemo(() => {
    if (!active) {
      return null;
    }

    <Box>NEW BOX</Box>


    if (active.id === 'activities') {
      return (<Box sx={root}><Activities /></Box>);
    } else if (active.id === 'dialob') {
      return (<Box sx={root}><DialobList /></Box>);
    }


    if (entity) {
      return <Box sx={root}>no view implemented entity editor: {JSON.stringify(active)}</Box>
    }
    throw new Error("unknown view: " + JSON.stringify(active, null, 2));

  }, [active, site, entity]);
}
export { Main }


