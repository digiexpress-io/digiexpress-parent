import React from 'react';

import { Container as MuiContainer, Stack, Drawer, AppBar, Tab } from '@mui/material';
import FlipToFrontOutlinedIcon from '@mui/icons-material/FlipToFrontOutlined';


import { EveliAppTabs } from './EveliAppTabs';
import { EveliShell, EveliShellClassName, EveliShellMiniBarClassName, EveliShellLargeBarClassName, useEveliShell } from '../eveli-shell';
import { EveliFooter } from '../eveli-footer';
import { EveliLogin } from '../eveli-login';
import { EveliLocales } from '../eveli-locales';
import { TabsProvider } from '../api-tabs';
import { IconbarProvider } from '../api-iconbar';


const ToggleDrawer: React.FC<ContainerProps> = (components) => {
  const { toggleDrawer } = useEveliShell();
  const { secondary: UserButtons, toolbar: UserTabs } = components;
  
  return (
    <Drawer variant='permanent' className={EveliShellClassName}>
      <div className={EveliShellMiniBarClassName} >
        <Tab onClick={toggleDrawer} icon={<FlipToFrontOutlinedIcon />} />
        <UserTabs />
      </div>
      <div className={EveliShellLargeBarClassName}><UserButtons /></div>
    </Drawer>);
}


export interface ContainerProps {
  main: React.FC;
  secondary: React.FC;
  toolbar: React.FC;
};

export const EveliApp: React.FC<ContainerProps> = (components) => {
  const { main: UserContent } = components;

  return (
  <IconbarProvider>
    <TabsProvider>
      <EveliShell drawerOpen={true}>
        <ToggleDrawer {...components}/>

        <AppBar position='fixed' className={EveliShellClassName}>      
          <Stack spacing={1} direction='row'>
            <EveliLocales value={'en'} onClick={() => {}} />
            <EveliAppTabs />
            <EveliLogin />
          </Stack>
        </AppBar>

        <main role='main'>
          <MuiContainer><UserContent /></MuiContainer>
        </main>

        <footer role='footer'>
          <EveliFooter />
        </footer>
      </EveliShell>
    </TabsProvider>
  </IconbarProvider>);
}
