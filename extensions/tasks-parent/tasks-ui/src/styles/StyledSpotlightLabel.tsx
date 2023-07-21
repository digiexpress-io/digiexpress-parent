import React from 'react';
import { Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';


const StyledSpotlightLabel: React.FC<{ values: number, message: string }> = ({ values, message }) => {

  return (<Typography sx={{ ml: 1 }} variant='caption'><FormattedMessage id={message} values={{ values }} /></Typography>)
}

export default StyledSpotlightLabel;
