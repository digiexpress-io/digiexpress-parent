import React from 'react';
import { Avatar, useTheme } from '@mui/material';
import DoneIcon from '@mui/icons-material/Done';

export interface IndicatorPublishedProps {
  size: 'SMALL' | 'LARGE'
}

export const IndicatorPublished: React.FC<IndicatorPublishedProps> = (props) => {
  const theme = useTheme();

  if (props.size === 'SMALL') {
    return (
      <Avatar sx={{ height: 'auto', width: 'auto', backgroundColor: theme.palette.success.main }}>
        <DoneIcon fontSize='inherit' />
      </Avatar>
    )
  }
  return (
    <Avatar sx={{ height: '20pt', width: '20pt', backgroundColor: theme.palette.success.main }}>
      <DoneIcon fontSize='small' />
    </Avatar>)
}