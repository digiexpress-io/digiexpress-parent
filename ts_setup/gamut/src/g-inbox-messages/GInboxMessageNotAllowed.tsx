import React from 'react';
import { Box, Divider, Typography, useThemeProps } from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';
import LocalPhoneIcon from '@mui/icons-material/LocalPhone';
import EmailIcon from '@mui/icons-material/Email';

import { useIntl } from 'react-intl';
import { GInboxMessageNotAllowedRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';

export interface GInboxMessageNotAllowedProps { }


export const GInboxMessageNotAllowed: React.FC<GInboxMessageNotAllowedProps> = (initProps) => {
  const intl = useIntl();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();




  return (
    <GInboxMessageNotAllowedRoot className={classes.root}>
      <Box className={classes.msgNotAllowedContentSpacing}>
        <div className={classes.msgNotAllowedContentFlex}>
          <InfoIcon className={classes.msgNotAllowedIcon} />
          <Typography>{intl.formatMessage({ id: 'gamut.inbox.newMessage.notAllowed1' })}</Typography>
        </div>
        <Typography className={classes.msgNotAllowedContent}>{intl.formatMessage({ id: 'gamut.inbox.newMessage.notAllowed2' })}</Typography>
        <Typography className={classes.msgNotAllowedContent}>{intl.formatMessage({ id: 'gamut.inbox.newMessage.notAllowed.questions' })}</Typography>

        <div className={classes.msgNotAllowedContentSpacing} />
        <Divider />
        <div className={classes.msgNotAllowedContentSpacing} />

        <div className={classes.msgNotAllowedContentFlex}>
          <LocalPhoneIcon className={classes.msgNotAllowedIcon} /><Typography>{intl.formatMessage({ id: 'cust.phone' })}</Typography>
        </div>

        <div className={classes.msgNotAllowedContentFlex}>
          <EmailIcon className={classes.msgNotAllowedIcon} /><Typography>{intl.formatMessage({ id: 'cust.email' })}</Typography>
        </div>

      </Box>
    </GInboxMessageNotAllowedRoot>
  )
}