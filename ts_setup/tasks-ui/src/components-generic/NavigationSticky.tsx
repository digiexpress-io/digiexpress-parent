import React from 'react';
import { Stack, Toolbar, AppBar, Button, Typography, lighten, darken } from '@mui/material';
import { SxProps } from '@mui/system';
import { FormattedMessage } from 'react-intl';
import { wash_me } from 'components-colors';
import Burger from 'components-burger';

interface NavigationButtonProps {
  onClick: () => void,
  id: string,
  values: {} | undefined
  color: string,
  active: boolean | undefined
}

function getButtonStyles(color: string, active: boolean | undefined) {
  const backgroundColor = active ? color : lighten(color, 0.8);
  const sx: SxProps = {
    borderRadius: 10,
    boxShadow: "unset",
    backgroundColor,
    border: `1px solid ${darken(color, 0.1)}`,
    color: active ? wash_me : darken(color, 0.5),
    '&:hover': {
      backgroundColor: active ? color : lighten(color, 0.2),
      color: wash_me,
      border: `1px solid ${darken(color, 0.1)}`
    },
    ml: 1
  }
  return sx;
}



const NavigationButton: React.FC<NavigationButtonProps> = ({ active, color, onClick, id, values }) => {
  const sx = React.useMemo(() => getButtonStyles(color, active), [color, active]);

  return (
    <Button variant='outlined' sx={sx} onClick={onClick}>
      <Typography variant='caption' fontWeight='bolder'>
        <FormattedMessage id={id} values={values} />
      </Typography>
    </Button>);
}



const NavigationSticky: React.FC<{ children: React.ReactNode, extendedAppBar?: React.ReactNode }> = ({ children, extendedAppBar }) => {
  return (
    <Burger.Toolbar>
      <Stack direction='row' spacing={1} alignItems='center'>
        {children}
      </Stack>
      {extendedAppBar}
    </Burger.Toolbar>
  );
}

export { NavigationSticky, NavigationButton };

