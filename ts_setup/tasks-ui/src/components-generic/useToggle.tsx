import React from 'react';

export function useToggle<T>() {
  const [open, setOpen] = React.useState<boolean>(false);
  const [entity, setEntity] = React.useState<T>();

  function handleStart(entity?: T) {
    setOpen(true);
    setEntity(entity);
  };
  function handleEnd() { 
    setOpen(false);
    setEntity(undefined);
  };

  return { open: open ? true : false, handleStart, handleEnd, entity }
}