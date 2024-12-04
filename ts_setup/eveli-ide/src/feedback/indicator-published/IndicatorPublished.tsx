import React from 'react';
import { Avatar, useTheme } from '@mui/material';
import DoneIcon from '@mui/icons-material/Done';



export const IndicatorPublished: React.FC = () => {
  const theme = useTheme();

  return (
    <Avatar sx={{ height: '20pt', width: '20pt', backgroundColor: theme.palette.success.main }}>
      <DoneIcon fontSize='small' />
    </Avatar>)
}