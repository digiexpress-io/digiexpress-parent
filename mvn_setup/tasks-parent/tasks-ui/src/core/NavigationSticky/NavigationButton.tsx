import React from 'react';
import { Button, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';


const NavigationButton: React.FC<{ onClick: (event: React.MouseEvent<HTMLElement>) => void, id: string, values?: {} }> = ({ onClick, id, values }) => {
  return (
    <Button variant='outlined' sx={{ borderRadius: 10 }} onClick={onClick}>
      <Typography variant='caption' sx={{ color: 'text.primary' }}>
        <FormattedMessage id={id} values={values} />
      </Typography>
    </Button>
  )
}

export default NavigationButton;