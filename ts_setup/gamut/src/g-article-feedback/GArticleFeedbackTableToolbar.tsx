import React from 'react';
import { Toolbar, IconButton, Tooltip, Typography } from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';
import { FormattedMessage } from 'react-intl';




export const GArticleFeedbackTableToolbar: React.FC<{ className: string}> = (props) => {
  return (
    <Toolbar className={props.className}>
      <Typography component='div' variant='h6'>
        <FormattedMessage id='gamut.feedback.table.title'/>
      </Typography>
      <Tooltip title="Filter list">
        <IconButton>
          <FilterListIcon />
        </IconButton>
      </Tooltip>

    </Toolbar>
  );
}