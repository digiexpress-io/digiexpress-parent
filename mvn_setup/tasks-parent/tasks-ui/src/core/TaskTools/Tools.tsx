import React from 'react';
import { Box, TextField, InputAdornment, Button, Stack, Toolbar, AppBar, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import SearchIcon from '@mui/icons-material/Search';

import FilterNormal from './FilterNormal';
import FilterStatus from './FilterStatus';
import FilterOwners from './FilterOwners';
import FilterRoles from './FilterRoles';
import GroupBy from './GroupBy';
import client from '@taskclient';


//general button style for all menu options
const OptionButton: React.FC<{ onClick: (event: React.MouseEvent<HTMLButtonElement>) => void, label: string }> = ({ onClick, label }) => {
  return (
    <Button variant='outlined' sx={{ borderRadius: 10, borderColor: 'text.primary' }} onClick={onClick}>
      <Typography variant='caption' sx={{ color: 'text.primary' }}><FormattedMessage id={label} /></Typography>
    </Button>
  )
}

const SearchFieldBar: React.FC<{ onChange: (value: React.ChangeEvent<HTMLInputElement>) => void }> = ({ onChange }) => {
  return (
    <TextField

      InputProps={{
        sx: {
          borderRadius: 10,
          width: '40ch',
          height: '2rem',
          '&.MuiOutlinedInput-root': {
            backgroundColor: 'mainContent.main',
            '&.Mui-focused fieldset': {
              borderColor: 'uiElements.main',
              borderWidth: '1px'
            }
          }
        },
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon sx={{ fontSize: '20px', color: 'uiElements.main' }} />
          </InputAdornment>
        )
      }}
      variant='outlined'
      placeholder='Search'
      onChange={onChange}
    />
  );
}



const Tools: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const ctx = client.useTasks();

  return (<>
    <AppBar color='inherit' position='sticky' sx={{ boxShadow: 1 }}>
      <Toolbar sx={{ backgroundColor: 'table.main', '&.MuiToolbar-root': { p: 1, m: 0 } }}>
        <Stack direction='row' spacing={1} alignItems='center'>
          <SearchFieldBar onChange={({ target }) => { ctx.setState(prev => prev.withSearchString(target.value)) }} />
          <Stack direction='row' spacing={1}>
            <GroupBy />
            <FilterStatus />
            <FilterNormal />
            <FilterOwners />
            <FilterRoles />
            <OptionButton label='core.search.searchBar.columns' onClick={() => console.log("TODO show/hide selected table columns")} />
          </Stack>
        </Stack>
      </Toolbar>
    </AppBar >

    <Box sx={{ pt: 3 }}></Box>

    <Box>{children} </Box>
    <Box sx={{ pt: 80 }}></Box>
  </>);
}

export { Tools };
