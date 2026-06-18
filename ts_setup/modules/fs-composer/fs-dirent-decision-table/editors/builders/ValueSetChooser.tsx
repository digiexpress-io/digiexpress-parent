import React from 'react'
import { List, ListItem, ListItemButton, ListItemText, ListItemIcon, Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl'

import { DeleteOutline as DeleteOutlineIcon } from '@mui/icons-material';
import * as Burger from '@dxs-ts/eveli-primitives';
import { StringBuilder } from './'


export const ValueSetChooser: React.FC<{ builder: StringBuilder, valueSet: string[], onChange: (value: string) => void }> = (props) => {
  const values = props.builder.getValues();

  const handleOperatorChange = (value: string) => {
    props.onChange(props.builder.withOperator(value))
  }

  const handleAddValue = (value?: string) => {
    if (value) {
      props.onChange(props.builder.withNewValue(value))
    }
  }

  const handleRemoveValue = (id: number) => {
    props.onChange(props.builder.remove(id));
  }

  const list = values.map((value, index) => (
    <ListItem disablePadding key={index}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <ListItemButton onClick={() => handleRemoveValue(index)} sx={{ justifyContent: 'center' }}>
          <ListItemIcon sx={{ minWidth: 'auto' }}>
            <DeleteOutlineIcon />
          </ListItemIcon>
        </ListItemButton>
        <ListItemText primary={value} />
      </Box>
    </ListItem>
  ));


  return (
    <>
      <Burger.Select
          label="decisions.cells.newvalue.string.comparisonType"
          onChange={handleOperatorChange}
          selected={props.builder.getOperator()}
          empty={{ id: '', label: 'decisions.cells.newvalue.string.empty' }}
          items={props.builder.operators.map((v) => ({
          id: v.value,
          value: (<ListItemText primary={v.text} />)
          }))}
      />
      <Typography><FormattedMessage id='decisions.cells.newvalue.string.available' />:</Typography>
      <List>
        {props.valueSet.map((v) => (
          <ListItem disablePadding key={v}>
            <ListItemButton onClick={() => handleAddValue(v)}>
                <ListItemText primary={v} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      <Typography><FormattedMessage id='decisions.cells.newvalue.string.selected.values' />:</Typography>
      {values.length > 0 ? <List>{list}</List> : <Typography><FormattedMessage id='decisions.cells.newvalue.string.selected.empty' /></Typography>}
    </>
  );
}
