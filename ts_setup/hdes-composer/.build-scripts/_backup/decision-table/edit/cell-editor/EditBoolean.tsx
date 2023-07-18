import React from 'react'
import { InputLabel, Select, MenuItem, FormControl } from '@material-ui/core';
import { FormattedMessage } from 'react-intl'
import { BooleanBuilder } from '../builders'


export const EditBoolean: React.FC<{ builder: BooleanBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {

  return (<FormControl variant="filled" fullWidth>
    <InputLabel><FormattedMessage id='dt.cell.newvalue.boolean' /></InputLabel>
    <Select autoWidth
      value={builder.value}
      onChange={({ target }) => onChange(target.value)}
      label={<FormattedMessage id='dt.cell.newvalue.boolean' />}>
      {builder.getDataTypes().map(v => (<MenuItem key={v.value} value={v.value}>{v.text}</MenuItem>))}
    </Select>
  </FormControl>);

}
