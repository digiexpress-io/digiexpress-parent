import React from 'react'
import { InputLabel, Select, MenuItem, TextField, Grid, FormControl } from '@material-ui/core';
import { FormattedMessage } from 'react-intl'
import { DateBuilder } from '../builders'


export const EditDate: React.FC<{ builder: DateBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {
  // yyyy-mm-dd 2017-07-03
  // equals / before / after / between


  const operator = (<FormControl fullWidth>
    <InputLabel><FormattedMessage id='dt.cell.date.operator' /></InputLabel>
    <Select autoWidth
      value={builder.getOperator()}
      onChange={({ target }) => onChange(builder.withOperator(target.value))}
      label={<FormattedMessage id='dt.cell.date.operator' />}
    >
      {builder.getOperators().map(v => (<MenuItem key={v.key} value={v.value}>{v.text}</MenuItem>))}
    </Select>
  </FormControl>)

  const start = (
    <TextField type='date' fullWidth 
      label={<FormattedMessage id='dt.cell.date' />}
      value={builder.getStart()}
      onChange={({ target }) => onChange(builder.withStart(target.value))} />
  );

  if (builder.getOperator() !== 'between') {
    return (
      <Grid container spacing={2}>
        <Grid item xs={3}>{operator}</Grid>
        <Grid item xs={9}>{start}</Grid>
      </Grid>
    );
  }

  const end = (<TextField type='date' fullWidth
    label={<FormattedMessage id='dt.cell.date' />}
    value={builder.getEnd()}
    onChange={({ target }) => onChange(builder.withEnd(target.value))} />)

  return (<Grid container spacing={2}>
    <Grid item xs={3}>{operator}</Grid>
    <Grid item xs={4}>{start}</Grid>
    <Grid item xs={5}>{end}</Grid>
  </Grid>);
}
