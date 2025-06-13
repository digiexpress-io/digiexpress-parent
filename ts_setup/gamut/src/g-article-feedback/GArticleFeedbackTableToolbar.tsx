import React from 'react';
import { Toolbar, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';




export const GArticleFeedbackTableToolbar: React.FC<{ className: string }> = (props) => {
  return (
    <Toolbar className={props.className}>
      <Typography component='div'><FormattedMessage id='gamut.feedback.table.title'/></Typography>
    </Toolbar>
  );
}