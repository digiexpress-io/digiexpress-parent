import React from 'react'

import Burger from '@the-wrench-io/react-burger';
import { DateBuilder } from './'


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

  return (<Burger.DateTimeField
    label="decisions.cells.newvalue.date.value"
    value={getMenuValue()}
    onChange={handleChange} />);

}
