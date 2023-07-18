import React from 'react'
import { TextField } from '@material-ui/core';
import { FormattedMessage } from 'react-intl'
import { DateBuilder } from '../builders'


export const EditDateSimple: React.FC<{ builder: DateBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {
  const getMenuValue = () => {
    const value = builder.value
    if (value) {
      return builder.getISODateString(value)
    }
    let isoStr = new Date().toISOString();
    return isoStr.substring(0, isoStr.length - 8)
  }

  return (<TextField fullWidth type='date'
      label={<FormattedMessage id='dt.cell.date' />}
      value={getMenuValue()}
      onChange={({ target }) => onChange(builder.getISODate(target.value))} />);
}
