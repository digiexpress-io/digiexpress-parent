import React from 'react';
import { TextField } from '@mui/material';
import Client from 'client';
import Context from 'context';



const Title: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();

  function handleTitleChange(event: React.ChangeEvent<HTMLInputElement>) {
    setState((current) => current.withTask({ ...state.task.entry, title: event.target.value }));
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    fullWidth
    value={state.task.title}
    onChange={handleTitleChange}
  />);
}



const Fields = { Title };
export default Fields;
