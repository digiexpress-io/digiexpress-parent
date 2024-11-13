import React from 'react';
import { Avatar, Grid, Typography, useThemeProps } from '@mui/material';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { GDate } from '../g-date';
import { GInboxMessageRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';



export interface GInboxMessageProps {
  created: DateTime;
  commentText: string;
  senderName: string;
  isMyMessage: boolean;
}

export const GInboxMessage: React.FC<GInboxMessageProps> = (initProps) => {
  const intl = useIntl();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();
  const { created, commentText, senderName, isMyMessage = {} } = props;

  const firstInitial = senderName.substring(0, 1);


  return (
    <GInboxMessageRoot className={classes.msgItemRoot}>
      <Grid container>
        <Grid item xs={12} sm={9} md={9} lg={9} xl={9} className={classes.msgItemSender}>
          {isMyMessage ? <Avatar className={classes.msgItemMyMessage}>{firstInitial}</Avatar> :
            <Avatar className={classes.msgItemTheirMessage}>{firstInitial}</Avatar>}
          <Typography>
            {senderName ? senderName : intl.formatMessage({ id: 'cust.inbox.message.sender-name.org-user' })} 
          </Typography>
        </Grid>

        <Grid item xs={12} sm={3} md={3} lg={3} xl={3} className={classes.msgItemSentat}>
          <Typography>
            <GDate variant='date-time' date={created} />
          </Typography>
        </Grid>

        <Grid item xs={12} sm={9} md={9} lg={9} xl={9} className={classes.msgItemCommentText}>
          <Typography>
            {commentText}
          </Typography>
        </Grid>
      </Grid>
    </GInboxMessageRoot>
  )
}

