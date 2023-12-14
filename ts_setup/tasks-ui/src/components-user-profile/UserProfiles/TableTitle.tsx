
import React from 'react';
import { Button, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { UserProfileSearchState } from './table-ctx';

const TableSubTitle: React.FC<{ values: number, message: string }> = ({ values, message }) => {

  return (<Typography sx={{ ml: 1 }} variant='caption'><FormattedMessage id={message} values={{ values }} /></Typography>)
}


const TableTitle: React.FC<{ group: UserProfileSearchState }> = ({ group }) => {
  const sx = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset", fontWeight: 'bolder' };
  return (<Button color="primary" variant="contained" sx={sx}>
    <FormattedMessage id={`userprofileTable.header.spotlight.results`} />
  </Button>);
}

export { TableSubTitle, TableTitle };


