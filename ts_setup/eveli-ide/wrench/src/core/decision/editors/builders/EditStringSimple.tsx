import React from 'react'
import { TextareaAutosize } from '@mui/material';
import { StringBuilder } from './'


export const EditStringSimple: React.FC<{ builder: StringBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {
  return (<TextareaAutosize minRows={10}
    style={{ width: '100%', height: '100%' }}
    value={builder.value}
    onChange={({ target }) => onChange(target.value)}
  />);
}
