import React from 'react';

import { Container as MuiContainer, Stack, Drawer, AppBar, IconButton, Typography } from '@mui/material';
import MenuOutlinedIcon from '@mui/icons-material/MenuOutlined';
import { FormattedMessage } from 'react-intl';


import { EveliShell, EveliShellClassName, EveliShellMiniBarClassName, EveliShellLargeBarClassName, EveliShellMiniBarTopClassName, useEveliShell } from '../eveli-shell';
import { EveliFooter } from '../eveli-footer';
import { EveliLogin } from '../eveli-login';
import { EveliLocales } from '../eveli-locales';

interface ContainerProps {
  main: React.ReactElement;
  secondary: React.ReactElement;
  toolbar: React.ReactElement;
};

const ToggleDrawer: React.FC<ContainerProps> = (components) => {
  const { toggleDrawer } = useEveliShell();
  const { secondary, toolbar } = components;


  return (
    <Drawer variant='permanent' className={EveliShellClassName}>
      <div className={EveliShellMiniBarClassName}>
        <div className={EveliShellMiniBarTopClassName}>
          <IconButton onClick={toggleDrawer}><MenuOutlinedIcon /></IconButton>
          <Typography variant='caption'><FormattedMessage id='toolbar.menu' /></Typography>
        </div>
        {toolbar}
      </div>

      <div className={EveliShellLargeBarClassName}>{secondary}</div>
    </Drawer>);
}


const Container: React.FC<ContainerProps> = (components) => {
  const { main } = components;

  return (<EveliShell drawerOpen={true}>
    <ToggleDrawer {...components} />

    <AppBar position='fixed' className={EveliShellClassName}>
      <Stack spacing={1} direction='row'>
        <EveliLocales value={'en'} onClick={() => { }} />
        <EveliLogin />
      </Stack>
    </AppBar>

    <main role='main'>
      <MuiContainer>{main}</MuiContainer>
    </main>

    <footer role='footer'>
      <EveliFooter />
    </footer>
  </EveliShell>);
}

export type { ContainerProps };
export { Container };
