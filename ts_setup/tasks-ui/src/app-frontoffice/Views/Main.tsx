import React from 'react';
import { Box } from '@mui/material';
import { SxProps } from '@mui/system';


import Burger from 'components-burger';
import Context from 'context';
import { CurrentTenant, DialobList } from 'components-tenant';
import SysConfig from 'components-sys-config';
import Tasks from 'components-task';
import Customer from 'components-customer';
import { CurrentUserProfile, UserProfiles } from 'components-user-profile';

import Activities from '../Activities';

const root: SxProps = { height: '100%', backgroundColor: "mainContent.main", width: '100%' };


const Main: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const { session } = Context.useComposer();
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
    } else if (active.id === 'taskSearch') {
      return (<Box sx={root}><Tasks.TaskSearch /></Box>);
    } else if (active.id === 'mytasks') {
      return (<Box sx={root}><Tasks.MyWork /></Box>);
    } else if (active.id === 'teamSpace') {
      return (<Box sx={root}><Tasks.TeamSpace /></Box>)
    } else if (active.id === 'inbox') {
      return (<Box sx={root}><Tasks.Inbox /></Box>)
    } else if (active.id === 'myoverview') {
      return (<Box sx={root}><Tasks.MyOverview /></Box>)
    } else if (active.id === 'customerSearch') {
      return (<Box sx={root}><Customer.CustomerSearch /></Box>);
    } else if (active.id === 'dialob') {
      return (<Box sx={root}><DialobList /></Box>);
    } else if (active.id === 'deployments') {
      return (<Box sx={root}><SysConfig /></Box>);
    } else if (active.id === 'dev') {
      return (<Box sx={root}><Tasks.Dev /></Box>);
    } else if (active.id === 'tenant') {
      return (<Box sx={root}><CurrentTenant /></Box>)
    } else if (active.id === 'currentUserProfile') {
      return (<Box sx={root}><CurrentUserProfile /></Box>)
    } else if (active.id === 'allUserProfiles') {
      return (<Box sx={root}><UserProfiles /></Box>)
    }
    else if (active.id === 'reporting') {

    }


    if (entity) {
      return <Box sx={root}>no view implemented entity editor: {JSON.stringify(active)}</Box>
    }
    throw new Error("unknown view: " + JSON.stringify(active, null, 2));

  }, [active, entity]);
}
export { Main }


