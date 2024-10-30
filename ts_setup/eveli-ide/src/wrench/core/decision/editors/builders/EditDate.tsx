import React from 'react'
import { ListItemText, Grid2 } from '@mui/material';

import * as Burger from '@/burger';

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
      <Grid2 container spacing={2}>
        <Grid2 size={{ xs: 3 }}>{operator}</Grid2>
        <Grid2 size={{ xs: 9 }}>{start}</Grid2>
      </Grid2>
    );
  }

  const end = (<Burger.DateField
    label="decisions.cells.newvalue.date.end"
    value={builder.getEnd()}
    onChange={(newEnd) => onChange(builder.withEnd(newEnd))} />)

  return (<Grid2 container spacing={2}>
    <Grid2 size={{ xs: 3 }}>{operator}</Grid2>
    <Grid2 size={{ xs: 4 }}>{start}</Grid2>
    <Grid2 size={{ xs: 5 }}>{end}</Grid2>
  </Grid2>);
}
