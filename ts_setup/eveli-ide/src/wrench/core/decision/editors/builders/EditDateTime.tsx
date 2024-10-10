import React from 'react'

import { ListItemText, Grid } from '@mui/material';

import Burger from '@/burger';
import { DateBuilder } from './'


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
  const operator = (
    <Burger.Select label="decisions.cells.newvalue.date.operator"
      helperText={"decisions.cells.newvalue.date.operator.helper"}
      selected={builder.getOperator()}
      onChange={handleOperatorChange}
      empty={{ id: '', label: 'decisions.cells.newvalue.date.operator.empty' }}
      items={builder.getOperators().map((type) => ({
        id: type.value,
        value: (<ListItemText primary={type.text} />)
      }))} />);

  const start = (<Burger.DateTimeField
    label="decisions.cells.newvalue.date.start"
    value={builder.getStart()}
    onChange={handleStartChange} />)

  if (builder.getOperator() !== 'between') {
    return (
      <Grid container spacing={2}>
        <Grid item xs={3}>{operator}</Grid>
        <Grid item xs={9}>{start}</Grid>
      </Grid>
    );
  }

  const end = (<Burger.DateTimeField
    label="decisions.cells.newvalue.date.end"
    value={builder.getEnd()}
    onChange={handleEndChange} />)

  return (<Grid container spacing={2}>
    <Grid item xs={3}>{operator}</Grid>
    <Grid item xs={4}>{start}</Grid>
    <Grid item xs={5}>{end}</Grid>
  </Grid>);
}
