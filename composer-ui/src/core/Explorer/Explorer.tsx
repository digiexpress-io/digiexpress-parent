import React from 'react';

import { Tabs, Tab, Box, TabProps, TabsProps, styled, alpha } from '@mui/material';

import { FormattedMessage } from 'react-intl';
import PublishIcon from '@mui/icons-material/Publish';
import HistoryIcon from '@mui/icons-material/History';
import AppsIcon from '@mui/icons-material/Apps';

import Burger from '@the-wrench-io/react-burger';

import DeClient from '@declient';
import Styles from '@styles';




const StyledTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    minHeight: theme.spacing(5),
    color: theme.palette.explorerItem.main,
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 'unset',
    paddingBottom: 'unset',
  },
  "&.Mui-selected": {
    //color: theme.palette.explorerItem.dark,
    width: "100%",
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
    "align-items": 'flex-start',
  }
}));



const Explorer: React.FC<{}> = () => {
  
  const { actions } = Burger.useTabs();
  const { session } = DeClient.useComposer();
  const [active, setActive] = React.useState<string>();

  const revisions = Object.values(session.head.projects);

  const handleActive = (_event: React.SyntheticEvent, newValue: string) => {
    setActive(newValue)
  }

  return (<Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: "100%", height: "100%", backgroundColor: "explorer.main" }}>
    <StyledTabs orientation="vertical" onChange={handleActive} value={active}>
      <StyledTab value='exporer.descriptors' icon={<AppsIcon />} 
        label={<FormattedMessage id="exporer.descriptors" />} 
        onClick={() => actions.handleTabAdd({id: 'descriptors', label: "project"})} />
      
      <StyledTab value='exporer.revisions' icon={<HistoryIcon />} label={<FormattedMessage id="exporer.revisions" />} />
      <StyledTab value='exporer.releases' icon={<PublishIcon />} label={<FormattedMessage id="exporer.releases" />} />
    </StyledTabs>
  </Box>);
}

export { Explorer };

