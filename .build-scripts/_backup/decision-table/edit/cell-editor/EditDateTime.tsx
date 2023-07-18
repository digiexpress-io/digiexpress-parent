import React from 'react'
import { FormattedMessage } from 'react-intl'
import { InputLabel, Select, MenuItem, TextField, FormControl, Grid } from '@material-ui/core';
import { DateBuilder } from '../builders'


export const EditDateTime: React.FC<{ builder: DateBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {

  const handleOperatorChange = (value: string) => {
    onChange(builder.withOperator(value));
  }

  const handleStartChange = (value: string) => {
    onChange(builder.withStart(value));
  }

  const handleEndChange = (value: string) => {
    onChange(builder.withEnd(value));
  }


  // yyyy-mm-dd'T'hh:mm:ss 2017-07-03T00:00:00
  // equals / before / after / between
  const operator = (<FormControl fullWidth>
    <InputLabel><FormattedMessage id='dt.cell.date.operator' /></InputLabel>
    <Select autoWidth
      value={builder.getOperator()}
      onChange={({ target }) => handleOperatorChange(target.value)}
      label={<FormattedMessage id='dt.cell.date.operator' />}>
      {builder.getOperators().map(v => (<MenuItem  key={v.value} value={v.value}>{v.text}</MenuItem>))}
    </Select>
  </FormControl>);

  const start = (<TextField fullWidth type='datetime-local'
    label={<FormattedMessage id='dt.cell.datetime' />}
    value={builder.getStart()}
    onChange={({ target }) => handleStartChange(target.value)} />)

  if (builder.getOperator() !== 'between') {
    return (
      <Grid container spacing={2}>
        <Grid item xs={3}>{operator}</Grid>
        <Grid item xs={9}>{start}</Grid>
      </Grid>
    );
  }

  const end = (<TextField label={<FormattedMessage id='dt.cell.datetime' />}
    type='datetime-local'
    value={builder.getEnd()}
    onChange={({ target }) => handleEndChange(target.value)} />)

  return (<Grid container spacing={2}>
    <Grid item xs={3}>{operator}</Grid>
    <Grid item xs={4}>{start}</Grid>
    <Grid item xs={5}>{end}</Grid>
  </Grid>);
}
