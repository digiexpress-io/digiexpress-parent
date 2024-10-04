import React from 'react';

import { Tabs as MuiTabs, Tab as MuiTab, useTheme, Box, SxProps } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

import * as API from '../context/tabs/TabsAPI';
import { useTabs } from '../context/tabs/TabsContext';

const tabStyles: SxProps = { minHeight: 'unset', color: "primary.dark", "&:focus": { color: "primary.main" } };
const tabsStyles: SxProps = {
  "& .MuiTabs-indicator": {
    backgroundColor: 'primary.main',
    marginRight: "49px"
  }
};
const closeStyles: SxProps = {
  m: 0,
  color: "primary.main",
  "&:hover": {
    color: "primary.dark"
  }
}

const Tab: React.FC<{
  key: number, 
  tab: API.TabSession<any>, 
  onClick: (e: React.ChangeEvent<{}>, tab: API.TabSession<any>) => void
}> = ({key, tab, onClick}) => {

  const icon = (<>
      {tab.icon ? tab.icon : null}
      <CloseIcon color="disabled" sx={closeStyles} onClick={(e) => onClick(e, tab)} />
      <Box component="span" sx={{ flexGrow: 1 }}></Box>
    </>);
  return (<MuiTab key={key} value={key} wrapped={true} label={tab.label} iconPosition="end" sx={tabStyles} icon={icon} />);
}

const Tabs: React.FC<{}> = () => {
  const { session, actions } = useTabs();
  const active = session.history.open;
  const tabs = session.tabs;

  return React.useMemo(() => {
    const handleTabChange = (_event: React.ChangeEvent<{}>, newValue: number) => {
      actions.handleTabChange(newValue);
    };
    const handleTabClose = (_event: React.ChangeEvent<{}>, newValue: API.TabSession<any>) => {
      _event.stopPropagation();
      actions.handleTabClose(newValue);
    };
    return (<MuiTabs value={active} onChange={handleTabChange} variant="scrollable" scrollButtons="auto" sx={tabsStyles}>
      {tabs.map((tab, index) => (<Tab key={index} tab={tab} onClick={handleTabClose}/>))}
    </MuiTabs >
    )
  }, [tabs, active, actions]);
}

export default Tabs;
