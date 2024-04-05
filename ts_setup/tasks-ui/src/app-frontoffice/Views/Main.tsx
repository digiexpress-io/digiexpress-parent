import React from 'react';
import { Box } from '@mui/material';
import { SxProps } from '@mui/system';

import Context from 'context';
import Burger from 'components-burger';
import { CurrentTenant, DialobList } from 'components-dialob';
import SysConfig from 'components-sys-config';
import Tasks from 'components-task';
import Customer from 'components-customer';
import { CurrentUserProfile, UserProfiles } from 'components-user-profile';
import { RolesOverview, DeOrgChart, PermissionsOverview, PrincipalsOverview } from 'components-access-mgmt';
import { wash_me } from 'components-colors';
import LoggerFactory from 'logger';

import Activities from '../Activities';

const log = LoggerFactory.getLogger();

const root: SxProps = { height: '100%', backgroundColor: wash_me, width: '100%' };

const Main: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const { session } = Context.useComposer();
  const tabs = layout.session.tabs;
  const active = tabs.length ? tabs[layout.session.history.open] : undefined;
  const entity = active ? session.getEntity(active.id) : undefined;
  log.debug("front office routing to tab", active?.id);

  //composers which are NOT linked directly with an article

  return React.useMemo(() => {
    if (!active) {
      return null;
    }

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
    } else if (active.id === 'rolesOverview') {
      return (<Box sx={root}><RolesOverview /></Box>)
    } else if (active.id === 'permissionsOverview') {
      return (<Box sx={root}><PermissionsOverview /></Box>)
    } else if (active.id === 'principalsOverview') {
      return (<Box sx={root}><PrincipalsOverview /></Box>)
    } else if (active.id === 'orgChart') {
      return (<Box sx={root}><DeOrgChart /></Box>)
    } else if (active.id === 'reporting') {
      return (<>reporting</>);
    }

    if (entity) {
      return <Box sx={root}>no view implemented entity editor: {JSON.stringify(active)}</Box>
    }
    throw new Error("unknown view: " + JSON.stringify(active, null, 2));

  }, [active, entity]);
}
export { Main }


