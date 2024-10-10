import React from 'react'
import { InputLabel, List, ListItem, ListItemButton, ListItemText, ListItemIcon, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl'

import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import Burger from '@/burger';
import { StringBuilder } from './'


export const ValueSetChooser: React.FC<{ builder: StringBuilder, valueSet: string[], onChange: (value: string) => void }> = (props) => {

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

  const list = props.builder.getValues()
    .map((value, index) => (
      <ListItem disablePadding key={index}>
        <Box display="flex">
          <Box flexGrow={0}>
            <ListItemButton onClick={() => handleRemoveValue(index)}>
              <ListItemIcon>
                <DeleteOutlineIcon />
              </ListItemIcon>
            </ListItemButton>
          </Box>
          <Box flexGrow={1}>
            <ListItemText primary={value} />
          </Box>
        </Box>
      </ListItem>));


  return (<>

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
    <InputLabel sx={{ mt: 1 }}><FormattedMessage id='decisions.cells.newvalue.string.available' /></InputLabel>
    {props.valueSet.map((v) => (
        <ListItemButton key={v} onClick={() => handleAddValue(v)}>
            <ListItemText primary={v} />
        </ListItemButton>
    ))}
    <InputLabel sx={{ mt: 1 }}><FormattedMessage id='decisions.cells.newvalue.string.selected' /></InputLabel>
    <List>{list}</List>

  </>);

}
