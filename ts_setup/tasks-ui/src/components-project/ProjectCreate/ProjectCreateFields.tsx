import React from 'react';
import { TextField } from '@mui/material';
import Context from 'context';



const Title: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();

  function handleTitleChange(event: React.ChangeEvent<HTMLInputElement>) {
    ctx.withTask({ ...ctx.task.entry, title: event.target.value });
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    fullWidth
    value={ctx.task.title}
    onChange={handleTitleChange}
  />);
}



const Fields = { Title };
export default Fields;
