import React from 'react'
import { ListItem, ListItemText, Typography, Divider } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';


export const SelectTask: React.FC<{ 
  value: Fs.WrenchAstBody<Fs.DecisionAst | Fs.FlowTaskAst>, 
  onClick: () => void, linked: boolean 
}> = ({ value, onClick, linked }) => {
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
