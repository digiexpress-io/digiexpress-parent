import React from 'react'

import * as Burger from '@/burger';

import { DateBuilder } from './'


export const EditDateSimple: React.FC<{ builder: DateBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {
  const getMenuValue = () => {
    const value = builder.value
    if (value) {
      return builder.getISODateString(value)
    }
    let isoStr = new Date().toISOString();
    return isoStr.substring(0, isoStr.length - 8)
  }

  return (<Burger.DateField
    label="decisions.cells.newvalue.date.value"
    value={getMenuValue()}
    onChange={(newStart) => onChange(builder.getISODate(newStart))} />)
}
