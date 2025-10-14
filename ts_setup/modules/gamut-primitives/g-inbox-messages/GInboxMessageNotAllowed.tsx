import React from 'react';
import { Box, Divider, Paper, Typography, useThemeProps } from '@mui/material';
import { Info as InfoIcon } from '@mui/icons-material';
import { LocalPhone as LocalPhoneIcon } from '@mui/icons-material';
import { Email as EmailIcon } from '@mui/icons-material';

import { useIntl } from 'react-intl';
import { MUI_NAME, useUtilityClasses } from './useUtilityClasses';

export interface GInboxMessageNotAllowedProps { }


export const GInboxMessageNotAllowed: React.FC<GInboxMessageNotAllowedProps> = (initProps) => {
  const intl = useIntl();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();




  return (
    <Paper className={classes.msgNotAllowed}>
      <Box className={classes.msgNotAllowedContentSpacing}>
        <div className={classes.msgNotAllowedContentFlex}>
          <InfoIcon className={classes.msgNotAllowedIconError} />
          <Typography>{intl.formatMessage({ id: 'gamut.inbox.newMessage.notAllowed1' })}</Typography>
        </div>
        <Typography className={classes.msgNotAllowedContent}>{intl.formatMessage({ id: 'gamut.inbox.newMessage.notAllowed2' })}</Typography>
        <Typography className={classes.msgNotAllowedContent}>{intl.formatMessage({ id: 'gamut.inbox.newMessage.notAllowed.questions' })}</Typography>

        <Divider />
        <div className={classes.msgNotAllowedContentSpacing} />
        <div className={classes.msgNotAllowedContentFlex}>
          <LocalPhoneIcon className={classes.msgNotAllowedIcon} /><Typography>{intl.formatMessage({ id: 'cust.phone' })}</Typography>
        </div>

        <div className={classes.msgNotAllowedContentFlex}>
          <EmailIcon className={classes.msgNotAllowedIcon} /><Typography>{intl.formatMessage({ id: 'cust.email' })}</Typography>
        </div>

      </Box>
    </Paper>
  )
}