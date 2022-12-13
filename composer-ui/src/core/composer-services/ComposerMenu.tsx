import React from "react";
import {
  Box, Typography, styled, Divider,
  AppBar, Toolbar, IconButton, Badge, InputBase, BadgeProps
} from "@mui/material";


import {
  BorderColor as BorderColorIcon,
  Build as BuildIcon,
  Menu as MenuIcon,
  Search as SearchIcon,
  RunningWithErrors as RunningWithErrorsIcon,
} from '@mui/icons-material';
import { FormattedMessage, useIntl } from 'react-intl';

import DeClient from '../DeClient';


const Search = styled('div')(({ theme }) => ({
  position: 'relative',
  borderRadius: theme.shape.borderRadius,
  marginRight: theme.spacing(2),
  marginLeft: 0,
  width: '100%',
}));

const SearchIconWrapper = styled('div')(({ theme }) => ({
  padding: theme.spacing(0, 2),
  height: '100%',
  position: 'absolute',
  pointerEvents: 'none',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
}));

const StyledInputBase = styled(InputBase)(({ theme }) => ({
  '& .MuiInputBase-input': {
    padding: theme.spacing(1, 1, 1, 0),
    paddingLeft: `calc(1em + ${theme.spacing(4)})`,
    transition: theme.transitions.create('width'),
    width: '100%',
  },
}));


const StyledBadge = styled(Badge)<BadgeProps>(({ theme }) => ({
  '& .MuiBadge-badge': {
    right: -14,
    top: 13,
    border: `2px solid ${theme.palette.background.paper}`,
    padding: '0 4px',
  },
}));

const ComposerMenu: React.FC<{ value: DeClient.ServiceDefinition }> = ({ value }) => {

  const stencil = value.refs.find(ref => ref.type === 'STENCIL')?.tagName;
  const hdes = value.refs.find(ref => ref.type === 'HDES')?.tagName;
  const version = {version: value.version.substring(0, 6), stencil, hdes}
  console.log("Service composer edit", version);
  
  return (<AppBar position="sticky" sx={{ backgroundColor: 'mainContent.main', boxShadow: 'unset', color: 'unset' }}>
    <Toolbar>
      <IconButton size="large" edge="start" color="inherit" sx={{ mr: 2 }}>
        <MenuIcon />
      </IconButton>

      <Search>
        <SearchIconWrapper><SearchIcon /></SearchIconWrapper>
        <StyledInputBase
          placeholder="Search…"
          inputProps={{ 'aria-label': 'search' }}
        />
      </Search>
      <Box sx={{ flexGrow: 1 }} />
      <Box sx={{ display: { xs: 'none', md: 'flex' } }}>

        <IconButton size="large" color="inherit" sx={{mr: 2}}>
          <StyledBadge badgeContent={version.stencil} color="success">
            <BorderColorIcon />
          </StyledBadge>
        </IconButton>
        <IconButton size="large" color="inherit" sx={{mr: 2}}>
          <StyledBadge badgeContent={version.hdes} color="success" >
            <BuildIcon />
          </StyledBadge>
        </IconButton>
        <IconButton size="large" color="inherit">
          <StyledBadge badgeContent={17} color="error">
            <RunningWithErrorsIcon />
          </StyledBadge>
        </IconButton>
      </Box>

    </Toolbar>
  </AppBar>);
}

export default ComposerMenu;
