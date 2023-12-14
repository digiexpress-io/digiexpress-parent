import React from 'react'
import { ListItemText, Grid } from '@mui/material';

import Burger from 'components-burger';

import { DateBuilder } from './'


export const EditDate: React.FC<{ builder: DateBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {
  // yyyy-mm-dd 2017-07-03
  // equals / before / after / between

  const operator = (
    <Burger.Select label="decisions.cells.newvalue.date.operator"
      helperText={"decisions.cells.newvalue.date.operator.helper"}
      selected={builder.value}
      onChange={(newOperator) => onChange(builder.withOperator(newOperator))}
      empty={{ id: '', label: 'decisions.cells.newvalue.date.operator.empty' }}
      items={builder.getOperators().map((type) => ({
        id: type.value,
        value: (<ListItemText primary={type.text} />)
      }))}
    />
  )

  const start = (
    <Burger.DateField
      label="decisions.cells.newvalue.date.start"
      value={builder.getStart()}
      onChange={(newStart) => onChange(builder.withStart(newStart))} />
  );

  if (builder.getOperator() !== 'between') {
    return (
      <Grid container spacing={2}>
        <Grid item xs={3}>{operator}</Grid>
        <Grid item xs={9}>{start}</Grid>
      </Grid>
    );
  }

  const end = (<Burger.DateField
    label="decisions.cells.newvalue.date.end"
    value={builder.getEnd()}
    onChange={(newEnd) => onChange(builder.withEnd(newEnd))} />)

  return (<Grid container spacing={2}>
    <Grid item xs={3}>{operator}</Grid>
    <Grid item xs={4}>{start}</Grid>
    <Grid item xs={5}>{end}</Grid>
  </Grid>);
}
