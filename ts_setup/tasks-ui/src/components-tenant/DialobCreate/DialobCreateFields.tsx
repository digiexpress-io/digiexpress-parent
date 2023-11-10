import React from 'react';
import { TextField } from '@mui/material';

const DialogName: React.FC<{}> = () => {
  const [dialogName, setDialogName] = React.useState('');

  function handleTitleChange(event: React.ChangeEvent<HTMLInputElement>) {
    setDialogName(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    fullWidth
    value={dialogName}
    onChange={handleTitleChange}
  />);
}

const TechnicalName: React.FC<{}> = () => {
  const [technicalName, setTechnicalName] = React.useState('');

  function handleTechnicalNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTechnicalName(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }}
    variant='standard'
    fullWidth
    multiline
    value={technicalName}
    onChange={handleTechnicalNameChange}
  />);
}


const Fields = { DialogName, TechnicalName };
export default Fields;
