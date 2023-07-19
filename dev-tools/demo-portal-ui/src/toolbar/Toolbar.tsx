import React from 'react';
import { Box, IconButton } from '@mui/material';

import MenuIcon from '@mui/icons-material/Menu';
import PersonIcon from '@mui/icons-material/Person';

import { Locale } from './Locale';
import { Logo } from './Logo';

import Portal from '@the-stencil-io/portal';


const OptionsComponent: React.FC<{}> = () => {
  const { actions, session } = Portal.useDrawer();
  return (<>
    <Box>
      <IconButton sx={{color: 'secondary.main'}}>
        <PersonIcon />
      </IconButton>
    </Box>
    <Box>
      <IconButton sx={{color: 'secondary.main'}} onClick={() => actions.handleDrawerOpen(!session.drawer)}>
        <MenuIcon />
      </IconButton>
    </Box>
  </>)
}


const ToolbarRow1: React.FC<{}> = () => {
  return (
    <Box display="flex" sx={{ backgroundColor: 'primary.main' }}>
      <Box sx={{ ml: 1 }}> John Smith </Box>
      <Box flexGrow={1} />
      <Locale />
    </Box>
  );
}


const ToolbarRow2: React.FC<{}> = () => {
  return (<Box display="flex" color="black" height="100%" >

    {/** COLUMN 1 */}
    <Box alignSelf="center" sx={{ color: 'primary.main', ml: 1 }} flexGrow={1}>
      <Logo />
    </Box>

    <Box flexGrow={1} />

    {/** COLUMN 2 */}
    <Box display="flex" alignSelf="center" >
      <OptionsComponent/>
    </Box>
  </Box>);
}

const Toolbar: React.FC<{}> = () => {

  return (
    <Box width="100%" height="100%" display="flex" flexDirection="column">
      <ToolbarRow1 />
      <ToolbarRow2 />
    </Box>
  )
}
export {Toolbar};