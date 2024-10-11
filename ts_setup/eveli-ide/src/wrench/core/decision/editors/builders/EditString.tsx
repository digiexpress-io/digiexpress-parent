import React from 'react'
import { InputLabel, List, ListItem, ListItemButton, ListItemText, ListItemIcon, Grid, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl'

import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import * as Burger from '@/burger';
import { StringBuilder } from './'


export const EditString: React.FC<{ builder: StringBuilder, onChange: (value: string) => void }> = (props) => {

  const [value, setValue] = React.useState<string>('');

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

    <Grid container spacing={2}>
      <Grid item xs={3}>
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
      </Grid>
      <Grid item xs={9}>
        <Burger.TextField
          label='decisions.cells.newvalue.string.addValue'
          value={value}
          onChange={setValue}
          onEnter={() => handleAddValue(value)} />
      </Grid>
    </Grid>

    <InputLabel sx={{ mt: 1 }}><FormattedMessage id='decisions.cells.newvalue.string.selected' /></InputLabel>
    <List>{list}</List>

  </>);

}
