import React from 'react';
import { ListItem, ListItemIcon, ListItemText } from '@mui/material';
import LinkIcon from '@mui/icons-material/Link';
import Portal from '../../';


interface LinkExternalProps {
  children: Portal.TopicLink
}

const LinkExternal: React.FC<LinkExternalProps> = ({ children }) => {
  const link = children;

  return (
    <ListItem button component="a" href={link.value} target="_blank">
      <ListItemIcon><LinkIcon /></ListItemIcon>
      <ListItemText primary={link.name} />
    </ListItem>)
}

export { LinkExternal }
