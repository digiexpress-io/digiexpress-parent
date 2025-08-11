import React from 'react';

import { Container as MuiContainer, Drawer, AppBar, IconButton, Typography, Divider } from '@mui/material';
import MenuOutlinedIcon from '@mui/icons-material/MenuOutlined';

import { 
  EveliShell, EveliShellClassName, 
  EveliShellMiniBarClassName, EveliShellMiniBarTopClassName, EveliShellLargeBarClassName, 
  useEveliShell, 
  EveliShellToolbarHeightOptions,
  EveliLogin, EveliShellExplorer, EveliLogo
} from '@dxs-ts/eveli-primitives';

import { IconbarProvider } from '@dxs-ts/eveli-api';
import { FormattedMessage } from 'react-intl';



const ToggleDrawer: React.FC<ContainerProps> = (components) => {
  const { toggleDrawer } = useEveliShell();
  const { secondary: UserButtons, toolbar: UserTabs } = components;

  return (
    <Drawer variant='permanent' className={EveliShellClassName}>
      <div className={EveliShellMiniBarClassName}>
        <div className={EveliShellMiniBarTopClassName}>
          <IconButton onClick={toggleDrawer}><MenuOutlinedIcon /></IconButton>
          <Typography variant='caption'><FormattedMessage id='toolbar.menu' /></Typography>
        </div>
        <UserTabs />
      </div>
      <div className={EveliShellLargeBarClassName}>
        <EveliLogo />

        <UserButtons />

        <EveliShellExplorer>
          <Divider />
          <EveliLogin />
        </EveliShellExplorer>
      </div>
    </Drawer>);
}


export interface ContainerProps {
  main: React.FC;
  secondary: React.FC;
  toolbar: React.FC;
  tabs?: React.FC;

  toolbarHeight?: Partial<EveliShellToolbarHeightOptions>;
  drawerWidth?: number;

  children?: React.ReactNode
};

export const EveliApp: React.FC<ContainerProps> = (components) => {
  const { main: UserContent, tabs: UserTabs } = components;

  return (
    <IconbarProvider>
      <EveliShell 
        drawerOpen={true} 
        toolbarHeight={components.toolbarHeight} 
        drawerWidth={components.drawerWidth} >
        
        <ToggleDrawer {...components} />

        <AppBar position='fixed' className={EveliShellClassName}>
          {UserTabs ? <UserTabs /> : <></>}
        </AppBar>

        <main role='main'>
          <MuiContainer><UserContent /></MuiContainer>
        </main>

        <>{components.children}</>
      </EveliShell>
    </IconbarProvider>);
}
