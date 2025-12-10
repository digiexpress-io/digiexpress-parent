import React from 'react'
import { List, ListItem, ListItemButton, ListItemText, ListItemIcon, Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl'

import { DeleteOutline as DeleteOutlineIcon } from '@mui/icons-material';

export const ValueSetChooserSimple: React.FC<{ 
  value?: string, 
  valueSet: string[], 
  onChange: (value: string) => void 
}> = (props) => {

  const handleSelectValue = (value: string) => {
    props.onChange(value);
  }

  return (
    <>
      <Typography><FormattedMessage id='decisions.cells.newvalue.string.available' />:</Typography>
      <List>
        {props.valueSet.map((v) => (
          <ListItem disablePadding key={v}>
            <ListItemButton onClick={() => handleSelectValue(v)}>
              <ListItemText primary={v} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      <Typography><FormattedMessage id='decisions.cells.newvalue.string.selected' />:</Typography>
      {props.value ? <Typography>{props.value}</Typography> : <Typography><FormattedMessage id='decisions.cells.newvalue.string.selected.empty' /></Typography>}
    </>
  );

}
