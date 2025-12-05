import React from 'react'
import { ListItem, ListItemText, Typography, Divider } from '@mui/material';
import { HdesApi } from '@dxs-ts/wrench-api';


export const SelectTask: React.FC<{ value:HdesApi.Entity<HdesApi.AstBody>, onClick: () => void, linked: boolean }> = ({ value, onClick, linked }) => {
  const { ast } = value;
  if (!ast) {
    return null;
  }
  return (<>
    <ListItem alignItems="flex-start" sx={{ cursor: "pointer" }} onClick={onClick}>
      <ListItemText
        primary={`${linked ? '* ' : ''}${ast.name}`}
        secondary={<Typography
          sx={{ display: 'inline' }}
          component="span"
          variant="body2"
          color="text.primary">
          {ast.description}
        </Typography>} />
    </ListItem>
    <Divider />
  </>);

}
