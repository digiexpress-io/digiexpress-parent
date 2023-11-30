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

const validateTehnicalName = (name: string, setError: (error: string) => void): void => {
  if (!name.length) {
    setError('dialob.form.technicalName.required');
  } else if (!/^[_\-a-zA-Z\d]*$/g.test(name)) {
    setError('dialob.form.technicalName.invalid');
  } else {
    setError('');
  }
}

const TextFields = { DialogName, TechnicalName, validateTehnicalName };
export default TextFields;
