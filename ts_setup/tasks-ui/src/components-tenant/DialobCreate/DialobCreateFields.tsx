import React from 'react';
import { TextField } from '@mui/material';

const DialogName: React.FC<{ value: string, onChange: (value: string) => void }> = ({ value, onChange }) => {

  return (
    <>
      <TextField InputProps={{ disableUnderline: true }} variant='standard'
        fullWidth
        value={value}
        onChange={({ target }) => onChange(target.value)}
      />
    </>
  );
}

const TechnicalName: React.FC<{ value: string, onChange: (value: string) => void }> = ({ value, onChange }) => {

  return (<TextField InputProps={{ disableUnderline: true }}
    variant='standard'
    fullWidth
    multiline
    value={value}
    onChange={({ target }) => onChange(target.value)}
  />);
}


const Fields = { DialogName, TechnicalName };
export default Fields;
