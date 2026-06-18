import React from 'react'
import { InputLabel, List, ListItem, ListItemButton, ListItemText, ListItemIcon, Grid2, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl'

import { DeleteOutline as DeleteOutlineIcon } from '@mui/icons-material';
import * as Burger from '@dxs-ts/eveli-primitives';
import { StringBuilder } from './'


export const EditString: React.FC<{ builder: StringBuilder, onChange: (value: string) => void }> = (props) => {
  const values = props.builder.getValues();
  const [value, setValue] = React.useState<string>('');

  const handleOperatorChange = (value: string) => {
    props.onChange(props.builder.withOperator(value))
  }

  const handleAddValue = (value?: string) => {
    if (value) {
      props.onChange(props.builder.withNewValue(value.trim()))
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
  
  <Box width="100%" display="flex" flexDirection="column" gap={2}>
    <Grid2 container spacing={2}>
      <Grid2 size={{ xs: 3 }}>
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
      </Grid2>
      <Grid2 size={{ xs: 3 }}>
        <Burger.TextField
          label='decisions.cells.newvalue.string.addValue'
          value={value}
          onChange={setValue}
          onEnter={() => handleAddValue(value)} />
      </Grid2>
    </Grid2>

    <Box>
      <InputLabel sx={{ mt: 1 }}><FormattedMessage id='decisions.cells.newvalue.string.selected.values' />:</InputLabel>
      {values.length > 0 ? <List>{list}</List> : <Box sx={{ mt: 1 }}><FormattedMessage id='decisions.cells.newvalue.string.selected.empty' /></Box>}
    </Box>
  </Box>
  );
}
