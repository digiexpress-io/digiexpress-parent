import React from 'react';

import { ListItem, ListItemText, ListItemIcon } from '@mui/material';
import CallIcon from '@mui/icons-material/Call';
import Portal from '../../';


interface LinkPhoneProps {
  children: Portal.TopicLink
}

const LinkPhone: React.FC<LinkPhoneProps> = ({ children }) => {
  const link = children;

  return (
    <ListItem>
      <ListItemIcon sx={{color: 'secondary.main'}}><CallIcon /></ListItemIcon>
      <ListItemText primary={`${link.name}: ${link.value}`} />
    </ListItem>)
}

export { LinkPhone }
