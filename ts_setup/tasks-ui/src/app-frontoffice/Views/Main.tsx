import React from 'react';
import { Box } from '@mui/material';
import { SxProps } from '@mui/system';

import Burger from 'components-burger';
import { CurrentTenant, DialobList } from 'components-dialob';
import SysConfig from 'components-release-mgmt';
import Tasks from 'components-task';
import { CustomerSearch } from 'components-customer';
import OrgChart from 'components-org-chart';
import { CurrentUserProfile, UserProfiles } from 'components-user-profile';
import { RolesOverview, PermissionsOverview, PrincipalsOverview } from 'components-access-mgmt';
import { wash_me } from 'components-colors';
import LoggerFactory from 'logger';

import Activities from '../Activities';
import { Playbooks } from 'components-libra';

const log = LoggerFactory.getLogger();

const root: SxProps = { height: '100%', backgroundColor: wash_me, width: '100%' };

const Main: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const tabs = layout.session.tabs;
  const active = tabs.length ? tabs[layout.session.history.open] : undefined;
  log.debug("front office routing to tab", active?.id);

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
      return (<Box sx={root}><CustomerSearch /></Box>);
    } else if (active.id === 'dialob') {
      return (<Box sx={root}><DialobList /></Box>);
    } else if (active.id === 'deployments') {
      return (<Box sx={root}><SysConfig /></Box>);
    } else if (active.id === 'dev') {
      return (<Box sx={root}><Tasks.Dev /></Box>);
    } else if (active.id === 'tenant') {
      return (<Box sx={root}><CurrentTenant /></Box>)
    } else if (active.id === 'myProfile') {
      return (<Box sx={root}><CurrentUserProfile /></Box>)
    } else if (active.id === 'allProfiles') {
      return (<Box sx={root}><UserProfiles /></Box>)
    } else if (active.id === 'allRoles') {
      return (<Box sx={root}><RolesOverview /></Box>)
    } else if (active.id === 'allPermissions') {
      return (<Box sx={root}><PermissionsOverview /></Box>)
    } else if (active.id === 'allPrincipals') {
      return (<Box sx={root}><PrincipalsOverview /></Box>)
    } else if (active.id === 'rolesOverview') {
      return (<Box sx={root}><OrgChart /></Box>)
    } else if (active.id === 'playbooks') {
      return (<Box sx={root}><Playbooks /></Box>)
    } else if (active.id === 'reporting') {
      return (<>reporting</>);
    }

    return <Box sx={root}>no view implemented entity editor: {JSON.stringify(active)}</Box>

  }, [active]);
}
export { Main }


