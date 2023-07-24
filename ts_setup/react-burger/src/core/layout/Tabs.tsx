import React from 'react';

import { Tabs as MuiTabs, Tab as MuiTab, useTheme, Box } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

import * as API from '../context/tabs/TabsAPI';
import { useTabs } from '../context/tabs/TabsContext';


const Tabs: React.FC<{}> = () => {
  const { session, actions } = useTabs();
  const theme = useTheme();
  const active = session.history.open;
  const tabs = session.tabs;

  return React.useMemo(() => {
    console.log("burger: render tabs", tabs);

    const handleTabChange = (_event: React.ChangeEvent<{}>, newValue: number) => {
      actions.handleTabChange(newValue);
    };
    const handleTabClose = (_event: React.ChangeEvent<{}>, newValue: API.TabSession<any>) => {
      _event.stopPropagation();
      actions.handleTabClose(newValue);
    };
    return (<MuiTabs value={active} onChange={handleTabChange} variant="scrollable" scrollButtons="auto"
      sx={{
        "& .MuiTabs-indicator": {
          backgroundColor: theme.palette.uiElements.main,
          marginRight: "49px"
        }
      }
      }
    >
      {
        tabs.map((tab, index) => (
          <MuiTab key={index} value={index} wrapped={true}
            label={tab.label}
            iconPosition="end"
            sx={{ minHeight: 'unset', color: "mainContent.dark", "&:focus": { color: "uiElements.main" } }}
            icon={(<>
              {tab.icon ? tab.icon : null}
              <CloseIcon color="disabled"
                onClick={(e) => handleTabClose(e, tab)}
                sx={{
                  m: 0,
                  color: "uiElements.main",
                  "&:hover": {
                    color: "mainContent.dark"
                  }
                }}
              />
              <Box component="span" sx={{ flexGrow: 1 }}></Box>
            </>)}
          />))
      }
    </MuiTabs >
    )
  }, [tabs, active, theme, actions]);
}

export default Tabs;
