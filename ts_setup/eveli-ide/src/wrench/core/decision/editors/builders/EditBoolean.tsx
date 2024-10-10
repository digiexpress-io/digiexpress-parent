import React from 'react'

import { ListItemText } from '@mui/material';

import Burger from 'import Burger from '@/burger'';
import { BooleanBuilder } from './'


export const EditBoolean: React.FC<{ builder: BooleanBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {
  return (
    <Burger.Select label="decisions.cells.newvalue.boolean"
      helperText={"decisions.cells.newvalue.boolean.helper"}
      selected={builder.value}
      onChange={onChange}
      empty={{ id: '', label: 'decisions.cells.newvalue.boolean.empty' }}
      items={builder.getDataTypes().map((type) => ({
        id: type.value,
        value: (<ListItemText primary={type.text} />)
      }))}
    />
  );
}
