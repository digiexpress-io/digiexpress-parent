import React from 'react';

import { makeStyles } from '@material-ui/styles';
import { Theme, ClickAwayListener, IconButton, Badge as MaterialBadge, Grow, Paper, Popper } from '@material-ui/core';


const useStyles = makeStyles((theme: Theme) => ({
  root: {
    color: theme.palette.primary.main,
    marginRight: 100,
  },
}));


interface ShellBadgeProps {
  open: boolean;
  badgeRef: HTMLElement|null,
  handleClose: () => void;
  children: BadgeValue;
}

const Badge: React.FC<ShellBadgeProps> = ({open, handleClose, children, badgeRef}) => {
  return (<Popper open={open} anchorEl={badgeRef} role={undefined} transition disablePortal>
  {({ TransitionProps, placement }) => (
    <Grow
      {...TransitionProps}
      style={{ transformOrigin: placement === 'bottom' ? 'center top' : 'center bottom' }}
    >
      <Paper>
        <ClickAwayListener onClickAway={handleClose}>
          <div>
            {children.onClick()}
          </div>
        </ClickAwayListener>
      </Paper>
    </Grow>
  )}
</Popper>);
}


interface BadgeValue {
  icon: React.ReactNode;
  label: string;
  onClick: () => React.ReactNode,
}

const Badges: React.FC<{children: BadgeValue[]}> = (props) => {
  const classes = useStyles();

  const [openBadge, setOpenBadge] = React.useState<number>(-1);
  const [badgeRef, setBadgeRef] = React.useState<HTMLElement|null>(null);  

  const handleBadgeOpen = (event: any, index: number) => {
    setOpenBadge(index);
    setBadgeRef(event.currentTarget);
  };

  const result = props.children.map((b, index) => (
    <IconButton key={index} color="inherit" className={classes.root} onClick={(event) => handleBadgeOpen(event, index)}>
      <MaterialBadge badgeContent={b.label} color="secondary">{b.icon}</MaterialBadge>
      <Badge badgeRef={badgeRef} open={index === openBadge} handleClose={() => setOpenBadge(-1)}>{b}</Badge>
    </IconButton>
  ))
  return (<>{result}</>);
}

export default Badges;
