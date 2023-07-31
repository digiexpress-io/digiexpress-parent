import React from 'react';
import { Box, InputBase, styled, alpha } from '@mui/material';

import SearchIcon from '@mui/icons-material/Search';


import Sticky from './Sticky';
import FilterNormal from './FilterNormal';
import FilterOwners from './FilterOwners';
import FilterRoles from './FilterRoles';
import GroupBy from './GroupBy';
import client from '@taskclient';



const Search = styled('div')(({ theme }) => ({
  position: 'relative',
  borderRadius: theme.shape.borderRadius,
  backgroundColor: alpha(theme.palette.common.black, 0.15),
  '&:hover': {
    backgroundColor: alpha(theme.palette.common.black, 0.25),
  },
  marginLeft: 0,
  width: '20%',
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
  color: 'inherit',
  '& .MuiInputBase-input': {
    padding: theme.spacing(1, 1, 1, 0),
    // vertical padding + font size from searchIcon
    paddingLeft: `calc(1em + ${theme.spacing(4)})`,
    transition: theme.transitions.create('width'),
    width: '100%'
  },
}));

const Tools: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const ctx = client.useTasks();

  return (<>
    <Sticky>
      <Search>
        <SearchIconWrapper><SearchIcon /></SearchIconWrapper>
        <StyledInputBase placeholder="Search…" onChange={({ target }) => {
          ctx.setState(prev => prev.withSearchString(target.value))
        }} />
      </Search>

      <GroupBy />
      <FilterNormal />
      <FilterOwners />
      <FilterRoles />

    </Sticky>

    <Box sx={{ pt: 10 }}></Box>

    <Box>{children} </Box>
    <Box sx={{ pt: 80 }}></Box>
  </>);
}

export { Tools };
