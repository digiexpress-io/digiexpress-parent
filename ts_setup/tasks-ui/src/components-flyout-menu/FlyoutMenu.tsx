import React from 'react';
import { Menu, ButtonGroup, Button, PopoverOrigin, MenuProps, styled, List, ButtonProps, Box } from '@mui/material';
import CheckIcon from '@mui/icons-material/Check';
import CloseIcon from '@mui/icons-material/Close';
import { grey_light_2, grey_light, grey } from 'components-colors';
import { FormattedMessage } from 'react-intl';
import { FlyoutMenuProvider, useFlyoutMenu } from './FlyoutMenuContext';



const StyledButton = styled(Button)<ButtonProps>(({ theme }) => ({
  backgroundColor: grey_light_2,
  color: grey,
  fontWeight: 'bold',
  border: 'unset',
  borderLeftColor: grey_light,

  '&:hover': {
    border: 'unset',
    backgroundColor: grey_light
  },
}));


const StyledMenu = styled(Menu)<MenuProps>(({ theme }) => ({
  
  ".MuiMenu-paper": {
    alignItems: 'center',
    display: 'flex',
    width: "500px",
    minHeight: "400px",
    borderRadius: '20px'
  },
  ".MuiMenu-list": {
    padding: 0,
    width: '100%' 
  }
}));

const transformOrigin: PopoverOrigin = {
  vertical: 'bottom',
  horizontal: 'center',
}

const anchorOrigin: PopoverOrigin = transformOrigin;

const FlyoutMenuDelegate: React.FC<FlyoutMenuProps> = ({ children, onCancel, onApply }) => {
  const ctx = useFlyoutMenu();
  function handleCancel() {
    if (onCancel) {
      onCancel();
    }
  }

  function handleApply() {
    if (onApply) {
      onApply();
    }
  }

  const [trigger, ...menuItems] = React.Children.toArray(children);

  return (<>
    {trigger}

    <StyledMenu
      transitionDuration={500}
      anchorEl={ctx.anchorEl}
      open={ctx.open}
      onClose={ctx.onClose}
      anchorOrigin={anchorOrigin}
      transformOrigin={transformOrigin}>

      <List sx={{ bgcolor: 'background.paper' }}>
        {menuItems}
      </List>


      {(onApply || onCancel) && (<ButtonGroup fullWidth>
        {onApply && <StyledButton onClick={handleApply}><CheckIcon /><FormattedMessage id="buttons.apply" /></StyledButton>}
        {onCancel && <StyledButton onClick={handleCancel}><CloseIcon /><FormattedMessage id="buttons.cancel" /></StyledButton>}
      </ButtonGroup>)
      }
    </StyledMenu>
  </>);
}

export type FlyoutMenuProps = {
  children: React.ReactNode;
  onCancel?: () => void | undefined;
  onApply?: () => void | undefined;
}
export const FlyoutMenu: React.FC<FlyoutMenuProps> = ({ children, onCancel, onApply }) => {
  return (<FlyoutMenuProvider><FlyoutMenuDelegate children={children} onCancel={onCancel} onApply={onApply} /></FlyoutMenuProvider>);
}