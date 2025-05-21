import React from 'react'
import { TextareaAutosize } from '@mui/material';
import { StringBuilder } from './'

export const EditStringSimple: React.FC<{ builder: StringBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {
  return (
    <TextareaAutosize
      minRows={8}
      maxRows={24}
      style={{
        width: '100%',
        resize: 'vertical',
      }}
      value={builder.value}
      onChange={({ target }) => onChange(target.value)}
    />
  );
}
