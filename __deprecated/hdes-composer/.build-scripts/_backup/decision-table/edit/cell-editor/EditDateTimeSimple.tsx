import React from 'react'
import { FormattedMessage } from 'react-intl'
import { TextField } from '@material-ui/core';
import { DateBuilder } from '../builders'


export const EditDateTimeSimple: React.FC<{ builder: DateBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {

  const handleChange = (value: string) => {
    onChange(builder.getISODate(value))
  }

  const getMenuValue = () => {
    let value = builder.value
    if (value) {
      return builder.getISODateString(value)
    }
    let isoStr = new Date().toISOString()
    return isoStr.substring(0, isoStr.length - 8)
  }

  return (<TextField label={<FormattedMessage id='dt.cell.datetime' />}
      type='datetime-local'
      value={getMenuValue()}
      onChange={({ target }) => handleChange(target.value)} />);

}
