import React from 'react';

import { IconButton, Box, Theme } from '@material-ui/core';
import { makeStyles } from '@material-ui/styles';

import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';


const useStyles = makeStyles((theme: Theme) => ({
  button: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: '0 8px',
    ...theme.mixins.toolbar,
  },
}));

interface HeaderLeftProps {
  children: React.ReactElement | undefined;
  onDrawerClose: () => void;
};

const HeaderLeft: React.FC<HeaderLeftProps> = ({ children, onDrawerClose }) => {
  const classes = useStyles();

  return (
    <div>
      <div>{children}</div>
      <Box flexGrow={1}/>
      <div className={classes.button}>
        <IconButton onClick={onDrawerClose}><ChevronLeftIcon /></IconButton>
      </div>
    </div>
  );
}

export type { HeaderLeftProps };
export {HeaderLeft};
