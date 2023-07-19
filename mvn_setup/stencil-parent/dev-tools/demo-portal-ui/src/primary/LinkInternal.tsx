import React from 'react';

import { ListItem, ListItemText, ListItemIcon } from '@mui/material';

import LinkIcon from '@mui/icons-material/Link';
import Portal from '@the-stencil-io/portal';


interface LinkInternalProps {
  children: Portal.TopicLink;
}

const LinkInternal: React.FC<LinkInternalProps> = ({ children }) => {
  const link = children;

  return (
    <ListItem button component="a" href={link.value} target="_blank">
      <ListItemIcon sx={{color: 'secondary.main'}}><LinkIcon /></ListItemIcon>
      <ListItemText primary={link.name} />
    </ListItem>)
}

export { LinkInternal }
