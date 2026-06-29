import React from 'react';
import { Box, Button, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { InHouseFillRoot, useUtilityClasses } from './useUtilityClasses';

interface InHouseFillProps {
  workflowName: string;
  onCancel: () => void;
}

export const InHouseFill: React.FC<InHouseFillProps> = ({ workflowName, onCancel }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const formName = decodeURIComponent(workflowName);

  return (
    <InHouseFillRoot className={classes.root}>
      <Box className={classes.content}>
        <Typography variant='h3'>{intl.formatMessage({ id: 'inHouseFill.prompt' }, { formName })}</Typography>
        <Typography variant='h3' className={classes.formName}>{formName}</Typography>

        <Box className={classes.spacer} />

        <Box className={classes.actions}>
          <Button variant='outlined' onClick={onCancel}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
          <Button variant='contained'>{intl.formatMessage({ id: 'button.startForm' })}</Button>
        </Box>
      </Box>
    </InHouseFillRoot>
  );
};
