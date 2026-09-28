import React from 'react';
import { Box, Typography } from '@mui/material';
import { CheckBox as CheckBoxIcon, CheckBoxOutlineBlank as CheckBoxOutlineBlankIcon } from '@mui/icons-material';
import { FormattedMessage } from 'react-intl';

import { GInputBaseAnyProps } from '../g-input-base';
import { GInputBooleanProps } from './GInputBoolean';
import { useUtilityClasses } from './useUtilityClasses';



export const ReadOnlyYesAndNoCheckbox: React.FC<GInputBaseAnyProps & GInputBooleanProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  const isYes = props.value === true;
  const isNo = props.value === false;

  return (
    <div className={classes.input}>
      <Box className={classes.option} sx={{ cursor: 'not-allowed', display: 'flex', alignItems: 'center', gap: 1 }}>
        {isYes ? <CheckBoxIcon className={classes.optionIcon} /> : <CheckBoxOutlineBlankIcon className={classes.optionIcon} />}
        <Typography className={classes.optionTitle}><FormattedMessage id='gamut.forms.answer.boolean.yes' /></Typography>
      </Box>
      <Box className={classes.option} sx={{ cursor: 'not-allowed', display: 'flex', alignItems: 'center', gap: 1 }}>
        {isNo ? <CheckBoxIcon className={classes.optionIcon} /> : <CheckBoxOutlineBlankIcon className={classes.optionIcon} />}
        <Typography className={classes.optionTitle}><FormattedMessage id='gamut.forms.answer.boolean.no' /></Typography>
      </Box>
    </div>
  );
}