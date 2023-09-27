import React from 'react';
import { Avatar, Stack, Typography } from '@mui/material';


const ActiveItem: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <Avatar sx={{ height: '30px', width: '30px', backgroundColor: "uiElements.main" }} variant='rounded'>
      <Typography variant='body2'>
        {children}
      </Typography>
    </Avatar>
  )
}


//possible look/design mock
const Pagination: React.FC = () => {

  return (
    <Stack direction='row' display='flex' alignItems='center' spacing={1} sx={{ p: 1 }}>
      <Typography variant='body2'>Items per page:</Typography>
      <Typography variant='body2'>10</Typography>
      <Typography variant='body2'>20</Typography>
      <Typography variant='body2'>30</Typography>
      <ActiveItem>All</ActiveItem>
    </Stack>
  )
}

export default Pagination;