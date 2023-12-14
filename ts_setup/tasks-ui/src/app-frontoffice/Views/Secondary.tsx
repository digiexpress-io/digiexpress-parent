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
import PieChartIcon from '@mui/icons-material/PieChart';
import WorkOutlineOutlinedIcon from '@mui/icons-material/WorkOutlineOutlined';
import MailOutlinedIcon from '@mui/icons-material/MailOutlined';
import FormatListBulletedOutlinedIcon from '@mui/icons-material/FormatListBulletedOutlined';
import CorporateFareOutlinedIcon from '@mui/icons-material/CorporateFareOutlined';

import { FormattedMessage } from 'react-intl';
import Burger from 'components-burger';
import Context from 'context';


const StyledTitleTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    color: theme.palette.explorerItem.main,
    fontSize: '9pt',
    paddingLeft: '.5rem',
    paddingRight: '.5rem',
  },
  "&.Mui-selected": {
    color: theme.palette.explorerItem.dark,
    backgroundColor: alpha(theme.palette.explorerItem.dark, .2),
  },
}));

const StyledBox = styled(Box)<BoxProps>(({ theme }) => ({
  borderBottom: `1px solid ${theme.palette.explorerItem.dark}`,
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
    color: theme.palette.explorerItem.main,
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 'unset',
    paddingBottom: 'unset',
  },
  "&.Mui-selected": {
    maxWidth: "unset",
    backgroundColor: alpha(theme.palette.explorerItem.dark, 0.2),
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
    color: theme.palette.explorerItem.main,
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 'unset',
    paddingBottom: 'unset',
  },
  "&.Mui-selected": {
    maxWidth: "unset",
    backgroundColor: alpha(theme.palette.explorerItem.dark, 0.2),
  },
  "& .MuiTab-iconWrapper": {
    marginBottom: 'unset',
    marginRight: theme.spacing(2)
  }
}));

const StyledTabs = styled(Tabs)<TabsProps>(({ theme }) => ({

  "& .MuiTabs-indicator": {
    backgroundColor: theme.palette.explorerItem.dark,
    width: '3px',
    right: 'unset'
  },
  "& .MuiTabs-flexContainerVertical": {
    "alignItems": 'flex-start',
  }
}));


const Secondary: React.FC<{}> = () => {
  const app = Context.useApp();
  const { actions } = Burger.useTabs();
  const [active, setActive] = React.useState<string>('explorer.taskSearch');


  function handleActive(_event: React.SyntheticEvent, newValue: string) { setActive(newValue) }
  function handleCRM() { actions.handleTabAdd({ id: 'crm', label: <FormattedMessage id="activities.frontoffice.crm.title" /> }) }
  function handleCustomerSearch() { actions.handleTabAdd({ id: 'customerSearch', label: <FormattedMessage id="activities.frontoffice.customerSearch.title" /> }) }

  function handleTasks() { actions.handleTabAdd({ id: 'tasks', label: <FormattedMessage id="activities.frontoffice.tasks.title" /> }) }
  function handleStencil() { app.changeApp("stencil") }
  
  function handleWrench() {  app.changeApp("hdes") }
  function handleDialob() { actions.handleTabAdd({ id: 'dialob', label: <FormattedMessage id="activities.frontoffice.dialob.title" /> }) }

  function handleGroup() { actions.handleTabAdd({ id: 'teamSpace', label: <FormattedMessage id="activities.teamSpace.title" /> }) }
  function handleMyTasks() { actions.handleTabAdd({ id: 'mytasks', label: <FormattedMessage id="activities.mytasks.title" /> }) }

  function handleMyHistory() { actions.handleTabAdd({ id: 'myhistory', label: <FormattedMessage id="activities.myhistory.title" /> }) }
  function handleTaskSearch() { actions.handleTabAdd({ id: 'taskSearch', label: <FormattedMessage id="activities.taskSearch.title" /> }) }
  function handleReporting() { actions.handleTabAdd({ id: 'reporting', label: <FormattedMessage id="activities.reporting.title" /> }) }
  function handleMyoverview() { actions.handleTabAdd({ id: 'myoverview', label: <FormattedMessage id="activities.myoverview.title" /> }) }
  function handleInbox() { actions.handleTabAdd({ id: 'inbox', label: <FormattedMessage id="activities.inbox.title" /> }) }
  function handleDeployments() { actions.handleTabAdd({ id: 'deployments', label: <FormattedMessage id="activities.deployments.title" /> }) }
  function handleOrg() { actions.handleTabAdd({ id: 'org', label: <FormattedMessage id="activities.org.title" /> }) }
  function handleTenant() { actions.handleTabAdd({ id: 'tenant', label: <FormattedMessage id="activities.frontoffice.crm.tenant.title" /> }) }
  function handleCurrentUserProfile() { actions.handleTabAdd({ id: 'currentUserProfile', label: <FormattedMessage id="activities.frontoffice.userProfile.title" /> }) }
  function handleAllUserProfiles() { actions.handleTabAdd({ id: 'allUserProfiles', label: <FormattedMessage id="activities.frontoffice.allUserProfiles.title" /> }) }


  React.useEffect(() => {
    handleTaskSearch();
  }, []);


  return (<Box sx={{ backgroundColor: "explorer.main", height: '100%', width: '100%' }}>
    <StyledBox>
      <StyledTitleTab label={<FormattedMessage id="explorer.title" />} value='label' />
    </StyledBox>
    <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: "100%", backgroundColor: "explorer.main" }}>
      <StyledTabs orientation="vertical" onChange={handleActive} value={active}>

        {/* material ui workaround for case when no tab is selected */}
        <EmptyTab value='explorer.taskSearch' />

        <StyledExplorerTab value='explorer.crm' label={<FormattedMessage id="explorer.frontoffice.crm.menuOption" />} onClick={handleCRM}
          icon={<AdminPanelSettingsOutlinedIcon fontSize='small' />} />

        <StyledExplorerSubTab value='explorer.customerSearch' label={<FormattedMessage id="explorer.frontoffice.crm.customerSearch.menuOption" />}
          onClick={handleCustomerSearch} icon={<SearchIcon fontSize='small' />} />

        <StyledExplorerTab value='explorer.tasks' label={<FormattedMessage id="explorer.frontoffice.tasks.menuOption" />} onClick={handleTasks}
          icon={<TaskAltIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorer.taskSearch' label={<FormattedMessage id="explorer.frontoffice.taskSearch.menuOption" />} onClick={handleTaskSearch}
          icon={<SearchIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorer.teamSpace' label={<FormattedMessage id="explorer.frontoffice.teamSpace.menuOption" />} onClick={handleGroup}
          icon={<GroupsIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorer.mytasks' label={<FormattedMessage id="explorer.frontoffice.mytasks.menuOption" />} onClick={handleMyTasks}
          icon={<WorkOutlineOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorer.inbox' label={<FormattedMessage id="explorer.frontoffice.inbox.menuOption" />} onClick={handleInbox}
          icon={<MailOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorer.myoverview' label={<FormattedMessage id="explorer.frontoffice.myoverview.menuOption" />} onClick={handleMyoverview}
          icon={<PieChartIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorer.myhistory' label={<FormattedMessage id="explorer.frontoffice.myhistory.menuOption" />} onClick={handleMyHistory}
          icon={<TimelineIcon fontSize='small' />} />

        <StyledExplorerTab value='explorer.config' label={<FormattedMessage id="explorer.frontoffice.config.menuOption" />} onClick={handleTasks}
          icon={<SettingsOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorer.stencil' label={<FormattedMessage id="explorer.frontoffice.stencil.menuOption" />} onClick={handleStencil}
          icon={<AbcOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorer.wrench' label={<FormattedMessage id="explorer.frontoffice.wrench.menuOption" />} onClick={handleWrench}
          icon={<BuildOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorerdialob' label={<FormattedMessage id="explorer.frontoffice.dialob.menuOption" />} onClick={handleDialob}
          icon={<ListAltOutlinedIcon fontSize='small' />} />

        <StyledExplorerSubTab value='explorer.deployments' label={<FormattedMessage id="explorer.frontoffice.deployments.menuOption" />} onClick={handleDeployments}
          icon={<FormatListBulletedOutlinedIcon fontSize='small' />} />

        <StyledExplorerTab value='explorer.administration' label={<FormattedMessage id="explorer.frontoffice.administration.menuOption" />} onClick={handleTasks}
          icon={<SupervisorAccountOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorer.currentUserProfile' label={<FormattedMessage id="explorer.frontoffice.userProfile.menuOption" />} onClick={handleCurrentUserProfile}
          icon={<PersonOutlineOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorer.allUserProfiles' label={<FormattedMessage id="explorer.frontoffice.allUserProfiles.menuOption" />} onClick={handleAllUserProfiles}
          icon={<PersonOutlineOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorer.org' label={<FormattedMessage id="explorer.frontoffice.org.menuOption" />} onClick={handleOrg}
          icon={<CorporateFareOutlinedIcon fontSize='small' />} />
        <StyledExplorerSubTab value='explorer.tenant' label={<FormattedMessage id="explorer.frontoffice.tenant.menuOption" />} onClick={handleTenant}
          icon={<CorporateFareOutlinedIcon fontSize='small' />} />

        <StyledExplorerTab value='explorer.reporting' label={<FormattedMessage id="activities.reporting.title" />} onClick={handleReporting}
          icon={<PieChartIcon fontSize='small' />} />

      </StyledTabs>
    </Box>
  </Box>)
}
export { Secondary }


