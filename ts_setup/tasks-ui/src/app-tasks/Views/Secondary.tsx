import React from 'react';
import { Tab, Box, TabProps, BoxProps, alpha, Tabs, TabsProps, styled } from '@mui/material';
import GroupsIcon from '@mui/icons-material/Groups';
import SearchIcon from '@mui/icons-material/Search';
import CrmIcon from '@mui/icons-material/AdminPanelSettingsOutlined';

import PieChartIcon from '@mui/icons-material/PieChart';
import TimelineIcon from '@mui/icons-material/Timeline';
import PersonIcon from '@mui/icons-material/Person';
import WorkOutlineOutlinedIcon from '@mui/icons-material/WorkOutlineOutlined';
import MailOutlinedIcon from '@mui/icons-material/MailOutlined';
import { FormattedMessage } from 'react-intl';
import Context from 'context';

import Burger from 'components-burger';
import { blueberry_whip, green_teal, sambucus } from 'components-colors';


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


const Secondary: React.FC<{}> = () => {

  const { actions } = Burger.useTabs();
  const { session } = Context.useComposer();
  const [active, setActive] = React.useState<string>('');

  function handleActive(_event: React.SyntheticEvent, newValue: string) { setActive(newValue) }
  function handleTeamSpace() { actions.handleTabAdd({ id: 'teamSpace', label: <FormattedMessage id="activities.teamSpace.title" /> }) }
  function handleMyTasks() { actions.handleTabAdd({ id: 'mytasks', label: <FormattedMessage id="activities.mytasks.title" /> }) }

  function handleMyHistory() { actions.handleTabAdd({ id: 'myhistory', label: <FormattedMessage id="activities.myhistory.title" /> }) }
  function handleSearch() { actions.handleTabAdd({ id: 'search', label: <FormattedMessage id="activities.search.title" /> }) }
  function handleSearchCustomers() { actions.handleTabAdd({ id: 'searchCustomers', label: <FormattedMessage id="activities.searchCustomers.title" /> }) }

  function handleReporting() { actions.handleTabAdd({ id: 'reporting', label: <FormattedMessage id="activities.reporting.title" /> }) }
  function handleMyoverview() { actions.handleTabAdd({ id: 'myoverview', label: <FormattedMessage id="activities.myoverview.title" /> }) }
  function handleInbox() { actions.handleTabAdd({ id: 'inbox', label: <FormattedMessage id="activities.inbox.title" /> }) }
  function handleDev() { actions.handleTabAdd({ id: 'dev', label: <FormattedMessage id="activities.dev.title" /> }) }

  return (<Box sx={{ backgroundColor: sambucus, height: '100%', width: '100%' }}>
    <StyledBox>
      <StyledTitleTab label={<FormattedMessage id="explorer.title" />} value='label' />
    </StyledBox>
    <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: "100%", backgroundColor: sambucus }}>
      <StyledTabs orientation="vertical" onChange={handleActive} value={active}>

        {/* material ui workaround for case when no tab is selected */}
        <EmptyTab value='' />

        <StyledExplorerTab value='explorer.search' icon={<SearchIcon />} label={<FormattedMessage id="activities.search.title" />} onClick={handleSearch} />
        <StyledExplorerTab value='explorer.searchCustomers' icon={<CrmIcon />} label={<FormattedMessage id="activities.searchCustomers.title" />} onClick={handleSearchCustomers} />
        <StyledExplorerTab value='explorer.teamSpace' icon={<GroupsIcon />} label={<FormattedMessage id="activities.teamSpace.title" />} onClick={handleTeamSpace} />

        <StyledExplorerTab value='explorer.mytasks-group' icon={<PersonIcon />} label={<FormattedMessage id="activities.mytasks.title" />} />
        <StyledExplorerSubTab value='explorer.mytasks' icon={<WorkOutlineOutlinedIcon />} label={<FormattedMessage id="activities.mytaskBoards.title" />} onClick={handleMyTasks} />
        <StyledExplorerSubTab value='explorer.inbox' icon={<MailOutlinedIcon />} label={<FormattedMessage id="activities.inbox.title" />} onClick={handleInbox} />
        <StyledExplorerSubTab value='explorer.myoverview' icon={<PieChartIcon />} label={<FormattedMessage id="activities.myoverview.title" />} onClick={handleMyoverview} />
        <StyledExplorerSubTab value='explorer.myhistory' icon={<TimelineIcon />} label={<FormattedMessage id="activities.myhistory.title" />} onClick={handleMyHistory} />

        <StyledExplorerTab value='explorer.reporting' icon={<PieChartIcon />} label={<FormattedMessage id="activities.reporting.title" />} onClick={handleReporting} />
        <StyledExplorerTab value='explorer.dev' icon={<PieChartIcon />} label={<FormattedMessage id="activities.dev.title" />} onClick={handleDev} />
      </StyledTabs>
    </Box>
  </Box>)
}
export { Secondary }


