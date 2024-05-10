import React from 'react';

export function useToggle() {
  const [open, setOpen] = React.useState(false);
  function handleStart() { setOpen(true) };
  function handleEnd() { setOpen(false) };

  return { open, handleStart, handleEnd }
}