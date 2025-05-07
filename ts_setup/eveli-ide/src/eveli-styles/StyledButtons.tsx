import React from 'react';
import { Button, ButtonProps } from '@mui/material';
import { FormattedMessage } from 'react-intl';


export const CancelButton: React.FC<ButtonProps & { labelId?: string }> = ({
  labelId = 'button.cancel',
  children,
  ...props
}) => (
  <Button variant="outlined" {...props}>
    {children || <FormattedMessage id={labelId} />}
  </Button>
);
