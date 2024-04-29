import React from 'react';
import { Tab, Box, TabProps, BoxProps, alpha, Tabs, TabsProps, styled } from '@mui/material';

import AdminPanelSettingsOutlinedIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import AbcOutlinedIcon from '@mui/icons-material/AbcOutlined';
import BuildOutlinedIcon from '@mui/icons-material/BuildOutlined';
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';
import ListAltOutlinedIcon from '@mui/icons-material/ListAltOutlined';
import SearchIcon from '@mui/icons-material/Search';
import GroupsIcon from '@mui/icons-material/Groups';
import TimelineIcon from '@mui/icons-material/Timeline';
import PersonOutlineOutlinedIcon from '@mui/icons-material/PersonOutlineOutlined';
import SupervisorAccountOutlinedIcon from '@mui/icons-material/SupervisorAccountOutlined';
import SecurityIcon from '@mui/icons-material/Security';
import DnsOutlinedIcon from '@mui/icons-material/DnsOutlined';
import PieChartIcon from '@mui/icons-material/PieChart';
import WorkOutlineOutlinedIcon from '@mui/icons-material/WorkOutlineOutlined';
import MailOutlinedIcon from '@mui/icons-material/MailOutlined';
import FormatListBulletedOutlinedIcon from '@mui/icons-material/FormatListBulletedOutlined';
import CorporateFareOutlinedIcon from '@mui/icons-material/CorporateFareOutlined';

import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import { blueberry_whip, green_teal, sambucus } from 'components-colors';
import { useApp } from './useApp';
import { useSecondaryMenuItem } from '../FrontofficePrefs';



const StyledTitleTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    color: blueberry_whip,
    fontSize: '9pt',
    paddingLeft: '.5rem',
    paddingRight: '.5rem',
  },
  "&.Mui-selected": {
    color: green_teal,
    backgroundColor: alpha(green_teal, .2),
  },
}));

const StyledBox = styled(Box)<BoxProps>(({ theme }) => ({
  borderBottom: `1px solid ${green_teal}`,
  width: '100%',
}));


const EmptyTab = styled(Tab)<TabProps>(() => ({
  display: "none"
}));

const StyledExplorerTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    minHeight: theme.spacing(3),
    fontSize: "12px",
    color: blueberry_whip,
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 'unset',
    paddingBottom: 'unset',
  },
  "&.Mui-selected": {
    maxWidth: "unset",
    backgroundColor: alpha(green_teal, 0.2),
  },
  "& .MuiTab-iconWrapper": {
    marginBottom: 'unset',
    marginRight: theme.spacing(2)
  }
}));
const StyledExplorerSubTab = styled(Tab)<TabProps>(({ theme }) => ({

  "&.MuiButtonBase-root": {
    paddingLeft: theme.spacing(5),
    minWidth: "unset",
    minHeight: theme.spacing(3),
    fontSize: "12px",
    color: blueberry_whip,
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 'unset',
    paddingBottom: 'unset',
  },
  "&.Mui-selected": {
    maxWidth: "unset",
    backgroundColor: alpha(green_teal, 0.2),
  },
  "& .MuiTab-iconWrapper": {
    marginBottom: 'unset',
    marginRight: theme.spacing(2)
  }
}));

const StyledTabs = styled(Tabs)<TabsProps>(({ theme }) => ({

  "& .MuiTabs-indicator": {
    backgroundColor: green_teal,
    width: '3px',
    right: 'unset'
  },
  "& .MuiTabs-flexContainerVertical": {
    "alignItems": 'flex-start',
  }
}));

export function useSecondary() {
  const app = useApp();
  const session = useSecondaryMenuItem();
  const { actions } = Burger.useTabs();
  const [active, setActive] = React.useState<string>(session.currentValue);

  const callbacks = {
    crm: function()              { session.setNextValue('customerSearch');  actions.handleTabAdd({ id: 'customerSearch',  label: <FormattedMessage id="activities.frontoffice.customerSearch.title" /> }) },
    stencil: function ()         { app.changeApp("stencil") },
    hdes: function ()            { app.changeApp("hdes") },
    dialob: function()           { session.setNextValue('dialob');          actions.handleTabAdd({ id: 'dialob',          label: <FormattedMessage id="activities.frontoffice.dialob.title" /> }) },
    teamSpace: function()        { session.setNextValue('teamSpace');       actions.handleTabAdd({ id: 'teamSpace',       label: <FormattedMessage id="activities.teamSpace.title" /> }) },

    mytasks: function()          { session.setNextValue('mytasks');         actions.handleTabAdd({ id: 'mytasks',         label: <FormattedMessage id="activities.mytasks.title" /> }) },
  
    myhistory: function()        { session.setNextValue('myhistory');       actions.handleTabAdd({ id: 'myhistory',       label: <FormattedMessage id="activities.myhistory.title" /> }) },
    taskSearch: function()       { session.setNextValue('taskSearch');      actions.handleTabAdd({ id: 'taskSearch',      label: <FormattedMessage id="activities.taskSearch.title" /> }) },
    reporting: function()        { session.setNextValue('reporting');       actions.handleTabAdd({ id: 'reporting',       label: <FormattedMessage id="activities.reporting.title" /> }) },
    myoverview: function ()      { session.setNextValue('myoverview');      actions.handleTabAdd({ id: 'myoverview',      label: <FormattedMessage id="activities.myoverview.title" /> }) },
    inbox: function ()           { session.setNextValue('inbox');           actions.handleTabAdd({ id: 'inbox',           label: <FormattedMessage id="activities.inbox.title" /> }) },
    releaseMgmt: function ()     { session.setNextValue('deployments');     actions.handleTabAdd({ id: 'deployments',     label: <FormattedMessage id="activities.deployments.title" /> }) },
  
    allRoles: function()         { session.setNextValue('allRoles');        actions.handleTabAdd({ id: 'allRoles',        label: <FormattedMessage id="activities.frontoffice.allRoles.title" /> }) },
    allPermissions: function ()  { session.setNextValue('allPermissions');  actions.handleTabAdd({ id: 'allPermissions',  label: <FormattedMessage id="activities.frontoffice.allPermissions.title" /> }) },
    allPrincipals: function ()   { session.setNextValue('allPrincipals');   actions.handleTabAdd({ id: 'allPrincipals',   label: <FormattedMessage id="activities.frontoffice.allUsers.title" /> }) },
  
    systemOverview: function ()  { session.setNextValue('systemOverview');  actions.handleTabAdd({ id: 'systemOverview',  label: <FormattedMessage id="activities.frontoffice.systemOverview.title" /> }) },
    tenant: function ()          { session.setNextValue('tenant');          actions.handleTabAdd({ id: 'tenant',          label: <FormattedMessage id="activities.frontoffice.crm.tenant.title" /> }) },
  
    myProfile: function ()       { session.setNextValue('myProfile');       actions.handleTabAdd({ id: 'myProfile',       label: <FormattedMessage id="activities.frontoffice.myProfile.title" /> }) },
    allProfiles: function ()     { session.setNextValue('allProfiles');     actions.handleTabAdd({ id: 'allProfiles',     label: <FormattedMessage id="activities.frontoffice.allProfiles.title" /> }) },
    rolesOverview: function ()   { session.setNextValue('rolesOverview');   actions.handleTabAdd({ id: 'rolesOverview',   label: <FormattedMessage id="activities.frontoffice.permissions.rolesOverview.title" /> }) }
  }

  return {callbacks, active, setActive} 
}


export const Secondary: React.FC<{ }> = () => {
  const session = useSecondaryMenuItem();
  const { callbacks, active, setActive } = useSecondary();
  function handleActive(_event: React.SyntheticEvent, newValue: string) { setActive(newValue) }


  React.useEffect(() => {
    if(session.currentValue) {
      const key: string = session.currentValue;
      const init = Object.entries(callbacks).filter(([name]) => name === key);
      if(init.length) {
        handleActive({} as any, key)
      }
    }
  }, []);

  return (<Box sx={{ backgroundColor: sambucus, height: '100%', width: '100%' }}>
    <StyledBox>
      <StyledTitleTab label={<FormattedMessage id="explorer.title" />} value='label' />
    </StyledBox>

    <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: "100%", backgroundColor: sambucus }}>
      <StyledTabs orientation="vertical" onChange={handleActive} value={active}>

        {/* material ui workaround for case when no tab is selected */}
        <EmptyTab value='explorer.taskSearch' />

        <StyledExplorerTab value='explorer.crm' label={<FormattedMessage id="explorer.frontoffice.crm.menuOption" />} onClick={undefined}
          icon={<AdminPanelSettingsOutlinedIcon fontSize='small' />} />

        <StyledExplorerSubTab
          value='customerSearch'
          label={<FormattedMessage id="explorer.frontoffice.crm.customerSearch.menuOption" />}
          onClick={callbacks.crm}
          icon={<SearchIcon fontSize='small' />} />

        <StyledExplorerTab
          value='explorer.tasks' label={<FormattedMessage id="explorer.frontoffice.tasks.menuOption" />}
          onClick={undefined}
          icon={<TaskAltIcon fontSize='small' />} />

        <StyledExplorerSubTab value='taskSearch' label={<FormattedMessage id="explorer.frontoffice.taskSearch.menuOption" />} onClick={callbacks.taskSearch}
          icon={<SearchIcon fontSize='small' />} />
        <StyledExplorerSubTab value='teamSpace' label={<FormattedMessage id="explorer.frontoffice.teamSpace.menuOption" />} onClick={callbacks.teamSpace}
          icon={<GroupsIcon fontSize='small' />} />
        <StyledExplorerSubTab value='mytasks' label={<FormattedMessage id="explorer.frontoffice.mytasks.menuOption" />} onClick={callbacks.mytasks}
          icon={<WorkOutlineOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='inbox' label={<FormattedMessage id="explorer.frontoffice.inbox.menuOption" />} onClick={callbacks.inbox}
          icon={<MailOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='myoverview' label={<FormattedMessage id="explorer.frontoffice.myoverview.menuOption" />} onClick={callbacks.myoverview}
          icon={<PieChartIcon fontSize='small' />} />
        <StyledExplorerSubTab value='myhistory' label={<FormattedMessage id="explorer.frontoffice.myhistory.menuOption" />} onClick={callbacks.myhistory}
          icon={<TimelineIcon fontSize='small' />} />

        <StyledExplorerTab value='explorer.assetMgmt' label={<FormattedMessage id="explorer.frontoffice.assetMgmt.menuOption" />} onClick={undefined}
          icon={<SettingsOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='stencil' label={<FormattedMessage id="explorer.frontoffice.stencil.menuOption" />} onClick={callbacks.stencil}
          icon={<AbcOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='hdes' label={<FormattedMessage id="explorer.frontoffice.hdes.menuOption" />} onClick={callbacks.hdes}
          icon={<BuildOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='dialob' label={<FormattedMessage id="explorer.frontoffice.dialob.menuOption" />} onClick={callbacks.dialob}
          icon={<ListAltOutlinedIcon fontSize='small' />} />

        <StyledExplorerTab value='explorer.deployments' label={<FormattedMessage id="explorer.frontoffice.deployments.menuOption" />} onClick={undefined}
          icon={<FormatListBulletedOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='releaseMgmt' label={<FormattedMessage id="explorer.frontoffice.releaseMgmt.menuOption" />} onClick={callbacks.releaseMgmt}
          icon={<FormatListBulletedOutlinedIcon fontSize='small' />} />

        <StyledExplorerTab value='userProfiles' label={<FormattedMessage id="explorer.frontoffice.userProfiles.menuOption" />} onClick={undefined}
          icon={<SupervisorAccountOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='myProfile' label={<FormattedMessage id="explorer.frontoffice.myProfile.menuOption" />} onClick={callbacks.myProfile}
          icon={<PersonOutlineOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='menuOption' label={<FormattedMessage id="explorer.frontoffice.allProfiles.menuOption" />} onClick={callbacks.allProfiles}
          icon={<PersonOutlineOutlinedIcon fontSize='small' />} />

        <StyledExplorerTab value='explorer.systemOverview' label={<FormattedMessage id="explorer.frontoffice.systemOverview.menuOption" />} onClick={undefined}
          icon={<SupervisorAccountOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='tenant' label={<FormattedMessage id="explorer.frontoffice.tenant.menuOption" />} onClick={callbacks.tenant}
          icon={<CorporateFareOutlinedIcon fontSize='small' />} />

        <StyledExplorerTab value='explorer.accessManagement' label={<FormattedMessage id="explorer.frontoffice.accessManagement.menuOption" />} onClick={undefined}
          icon={<SecurityIcon fontSize='small' />} />
        <StyledExplorerSubTab value='allRoles' label={<FormattedMessage id="explorer.frontoffice.allRoles.menuOption" />} onClick={callbacks.allRoles}
          icon={<DnsOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='allPrincipals' label={<FormattedMessage id="explorer.frontoffice.allUsers.menuOption" />} onClick={callbacks.allPrincipals}
          icon={<AdminPanelSettingsOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='allPermissions' label={<FormattedMessage id="explorer.frontoffice.allPermissions.menuOption" />} onClick={callbacks.allPermissions}
          icon={<AdminPanelSettingsOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='rolesOverview' label={<FormattedMessage id="explorer.frontoffice.rolesOverview.menuOption" />} onClick={callbacks.rolesOverview}
          icon={<AdminPanelSettingsOutlinedIcon fontSize='small' />} />
        <StyledExplorerTab value='reporting' label={<FormattedMessage id="activities.reporting.title" />} onClick={callbacks.reporting}
          icon={<PieChartIcon fontSize='small' />} />

      </StyledTabs>
    </Box>
  </Box>)
}