import React from 'react';

import { Tabs as MuiTabs, Tab as MuiTab, useTheme, Box, Stack } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

import { useTabs, OneTab } from '../api-tabs';


export const EveliAppTabs: React.FC<{}> = () => {
  const ctx = useTabs();
  const theme = useTheme();
  const active = ctx.session.history.open;
  const tabs = ctx.session.tabs;

  return React.useMemo(() => {

              
            
    const handleTabChange = (_event: React.ChangeEvent<{}>, newValue: number) => {
      ctx.handleTabChange(newValue);
    };
    const handleTabClose = (_event: React.ChangeEvent<{}>, newValue: OneTab<any>) => {
      _event.stopPropagation();
      ctx.handleTabClose(newValue);
    };
    return (
      <Stack spacing={1} direction='row'>
        <MuiTabs value={active} onChange={handleTabChange} variant="scrollable" scrollButtons="auto" >
          {
            tabs.map((tab, index) => (
              <MuiTab key={index} value={index} wrapped={true}
                label={tab.label}
                iconPosition="end"
                icon={(<>
                  {tab.icon ? tab.icon : null}
                  <CloseIcon color="disabled" onClick={(e) => handleTabClose(e, tab)}/>
                  <Box component="span" sx={{ flexGrow: 1 }}></Box>
                </>)}
              />))
          }
        </MuiTabs>
      </Stack>
    )
  }, [tabs, active, theme]);
}