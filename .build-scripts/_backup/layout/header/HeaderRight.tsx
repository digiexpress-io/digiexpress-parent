import React from 'react';
import clsx from 'clsx';
import { IconButton, Theme } from '@material-ui/core';
import { makeStyles } from '@material-ui/styles';

import MenuIcon from '@material-ui/icons/Menu';


const useStyles = makeStyles((theme: Theme) => ({
  menuButton: {
    color: theme.palette.primary.main,
  },
  menuButtonHidden: {
    display: 'none',
  }
}));

interface HeaderRightProps {
  drawer: {
    open: boolean;
    onOpen: () => void;  
  };
  children: React.ReactElement[];
};

const HeaderRight: React.FC<HeaderRightProps> = ({ children, drawer }) => {
  const classes = useStyles();

  return (<>
    <IconButton edge="start" color="inherit"
      className={clsx(classes.menuButton, drawer.open && classes.menuButtonHidden)}
      onClick={drawer.onOpen}>
      <MenuIcon />
    </IconButton>
    {children}
  </>);
}

export type { HeaderRightProps };
export { HeaderRight };
