import React from 'react';
import { Button, Typography, SxProps, lighten, darken } from '@mui/material';
import { FormattedMessage } from 'react-intl';


interface NavigationSearchType {
  onClick: (event: React.MouseEvent<HTMLElement>) => void,
  id: string,
  values: {} | undefined
}

const NavigationButtonSearch: React.FC<NavigationSearchType> = ({ onClick, id, values }) => {
  return (
    <Button variant='outlined' sx={{ borderRadius: 10, borderColor: 'rgba(96, 113, 150, 0.5)' }} onClick={onClick}>
      <Typography variant='caption' sx={{ color: 'text.primary', fontWeight: 'bolder' }}>
        <FormattedMessage id={id} values={values} />
      </Typography>
    </Button>
  )
}


interface NavigationDialobListType {
  onClick: () => void,
  id: string,
  values: {} | undefined
  color: string,
  active: boolean | undefined
}

const NavigationButtonDialobList: React.FC<NavigationDialobListType> = ({ active, color, onClick, id, values }) => {
  const backgroundColor = active ? color : lighten(color, 0.8);
  const sx: SxProps = {
    borderRadius: 10,
    boxShadow: "unset",
    backgroundColor,
    border: `1px solid ${darken(color, 0.1)}`,
    color: active ? 'mainContent.main' : darken(color, 0.5),
    '&:hover': {
      backgroundColor: active ? color : lighten(color, 0.2),
      color: 'mainContent.main',
      border: `1px solid ${darken(color, 0.1)}`
    },
    ml: 1
  };

  return (
    <Button variant='outlined' sx={sx} onClick={onClick}>
      <Typography variant='caption' fontWeight='bolder'>
        <FormattedMessage id={id} values={values} />
      </Typography>
    </Button>);
}



export { NavigationButtonSearch, NavigationButtonDialobList };