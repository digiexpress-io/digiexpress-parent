import React from 'react';
import { Box, Button, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFetch } from '@dxs-ts/envir-fetch';

import { InHouseFillRoot, useUtilityClasses } from './useUtilityClasses';
import { useNavigate } from '@tanstack/react-router';

interface InHouseFillProps {
  workflowName: string;
  locale: string;
  onCancel: () => void;
}

export const InHouseFillStart: React.FC<InHouseFillProps> = ({ workflowName, locale, onCancel }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const formName = decodeURIComponent(workflowName);
  const { getInHouseSession } = useFetch('worker/rest/api/tasks/in-house/$id.GET', {});
  const nav = useNavigate();


  async function handleStartForm() {
    const session = await getInHouseSession(workflowName, locale);
    nav({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/in-house-sessions/$workflowId',
      params: { workflowId: session.form.formSessionId }
    })
  }

  return (
    <InHouseFillRoot className={classes.root}>
      <Box className={classes.content}>
        <Typography variant='h3'>{intl.formatMessage({ id: 'inHouseFill.prompt' }, { formName })}</Typography>
        <Typography variant='h3' className={classes.formName}>{formName}</Typography>

        <Box className={classes.spacer} />

        <Box className={classes.actions}>
          <Button variant='outlined' onClick={onCancel}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
          <Button variant='contained' onClick={handleStartForm}>{intl.formatMessage({ id: 'button.startForm' })}</Button>
        </Box>
      </Box>
    </InHouseFillRoot>
  );
};
