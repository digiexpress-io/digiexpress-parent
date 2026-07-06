import React from 'react';

import { ArrowDropDown as ArrowDropDownIcon } from '@mui/icons-material';
import { Button, ButtonGroup, ClickAwayListener, Grow, MenuItem, MenuList, Paper, Popper } from '@mui/material';


import { useIntl } from 'react-intl';

import { DebugOptionType } from '../api';


export const DebugDrawer: React.FC<{

  onSelect: (type: DebugOptionType) => void;
}> = ({ onSelect }) => {
  const [open, setOpen] = React.useState(false);
  const anchorRef = React.useRef<HTMLDivElement>(null);
  const intl = useIntl();



  const handleToggle = () => {
    setOpen((prevOpen) => !prevOpen);
  };

  const handleClose = (event: Event) => {
    if (
      anchorRef.current &&
      anchorRef.current.contains(event.target as HTMLElement)
    ) {
      return;
    }

    setOpen(false);
  };

  return (
    <React.Fragment>
      <ButtonGroup variant="contained" ref={anchorRef}>
        <Button
          size="small"
          aria-controls={open ? 'split-button-menu' : undefined}
          aria-expanded={open ? 'true' : undefined}
          aria-haspopup="menu"
          onClick={handleToggle}
        >
          {intl.formatMessage({ id: 'debug.toolbar.options' })}
          <ArrowDropDownIcon />
        </Button>
      </ButtonGroup>
      <Popper
        sx={{ zIndex: 1 }}
        open={open}
        anchorEl={anchorRef.current}
        role={undefined}
        transition
        disablePortal
      >
        {({ TransitionProps, placement }) => (
          <Grow
            {...TransitionProps}
            style={{
              transformOrigin:
                placement === 'bottom' ? 'center top' : 'center bottom',
            }}
          >
            <Paper>
              <ClickAwayListener onClickAway={handleClose}>
                <MenuList id="split-button-menu" autoFocusItem>
                  <MenuItem onClick={() => onSelect('INPUT_CSV')}>
                    {intl.formatMessage({ id: 'debug.toolbar.inputCsv' })}
                  </MenuItem>

                  <MenuItem
                    onClick={() => onSelect('INPUT_FORM')}>
                    {intl.formatMessage({ id: 'debug.toolbar.inputForm' })}
                  </MenuItem>

                  <MenuItem onClick={() => onSelect('INPUT_JSON')}>
                    {intl.formatMessage({ id: 'debug.toolbar.inputJson' })}
                  </MenuItem>
                </MenuList>
              </ClickAwayListener>
            </Paper>
          </Grow>
        )}
      </Popper>
    </React.Fragment>
  );
}
