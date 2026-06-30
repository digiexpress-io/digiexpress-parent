import React from 'react';
import { Box, Button, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { InHouseFillRoot, useUtilityClasses } from './useUtilityClasses';

interface InHouseFillEndProps {
  onAccept: () => void;
}

export const InHouseFillEnd: React.FC<InHouseFillEndProps> = ({ onAccept }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <InHouseFillRoot className={classes.root}>
      <Box className={classes.content}>
        <Typography variant='h3'>{intl.formatMessage({ id: 'inHouseFill.prompt.end' })}</Typography>

        <Box className={classes.spacer} />

        <Box className={classes.actions}>
          <Button variant='contained' onClick={onAccept}>{intl.formatMessage({ id: 'button.accept' })}</Button>
        </Box>
      </Box>
    </InHouseFillRoot>
  );
};
