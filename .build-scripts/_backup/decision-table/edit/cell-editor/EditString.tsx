import React from 'react'
import { Select, InputLabel, List, ListItem, ListItemButton, ListItemText, ListItemIcon, MenuItem, TextField, Grid, FormControl, Box } from '@material-ui/core';
import { FormattedMessage } from 'react-intl'
import { StringBuilder } from '../builders'
import DeleteOutlineIcon from '@material-ui/icons/DeleteOutline';

export const EditString: React.FC<{ builder: StringBuilder, onChange: (value: string) => void }> = (props) => {

  const [value, setValue] = React.useState<string>('');

  const handleOperatorChange = (value: string) => {
    props.onChange(props.builder.withOperator(value))
  }

  const handleAddValue = (key: string, value?: string) => {
    if (key === 'Enter' && value) {
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
        <FormControl variant="filled" fullWidth>
          <InputLabel><FormattedMessage id='dt.cell.comparison' /></InputLabel>
          <Select
            onChange={({ target }) => handleOperatorChange(target.value)}
            value={props.builder.getOperator()}
            autoWidth
            label={<FormattedMessage id='dt.cell.comparison' />}>
            {props.builder.operators.map(v => (<MenuItem  key={v.value} value={v.value}>{v.text}</MenuItem>))}
          </Select>
        </FormControl>
      </Grid>
      <Grid item xs={9}>
        <TextField fullWidth
          label={<FormattedMessage id='dt.cell.newvalue.add' />}
          value={value}
          onChange={({ target }) => setValue(target.value)}
          onKeyPress={(event) => handleAddValue(event.key, value)} />
      </Grid>
    </Grid>

    <InputLabel sx={{ mt: 1 }}><FormattedMessage id='dt.cell.selected' /></InputLabel>
    <List>{list}</List>
  </>);

}
