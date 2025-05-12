import React from 'react';
import { Grid, Tooltip, Typography, useThemeProps } from '@mui/material';
import MarkEmailUnreadOutlinedIcon from '@mui/icons-material/MarkEmailUnreadOutlined';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { GDate } from '../g-date';
import { MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { useComms } from '../api-comms';


export interface GInboxItemProps {
  children: React.ReactNode
  id: string;
  title: string;
  taskRefId: string,
  subTitle: string;
  senderName: string;
  sentAt: DateTime;
  contractStatus: string;
  onClick: (subjectId: string) => void;
}

export const GInboxItem: React.FC<GInboxItemProps> = (initProps) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { getSubject } = useComms();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { title, subTitle, senderName, sentAt, onClick, id, taskRefId } = props;

  const tooltipContent = subTitle && intl.formatMessage({ id: 'gamut.inbox.newMessageFrom' }) + senderName + ": " + props.subTitle;
  const subject = getSubject(id);

  const isViewed = subject?.isViewed;

  return (
<<<<<<< HEAD
    <InboxItem container className={classes.inboxItem} onClick={() => onClick(id)}>

      <Grid item xs={6} sm={2} md={2} lg sx={{ flexGrow: 1 }} className={classes.taskRefLayout}>
=======
    <div className={classes.inboxItem} onClick={() => onClick(id)}>
      <Grid item xs={6} sm={2} md={2} lg={2} xl={2} className={classes.taskRefLayout}>
>>>>>>> 62f2a26cc18f4593f6f569dd84f624b733b4a5b8

        {isViewed ? <></> : <Tooltip title={tooltipContent}>
          <MarkEmailUnreadOutlinedIcon className={classes.newMsgIndicator} />
        </Tooltip>}
        <Typography variant='caption'>
          {intl.formatMessage({ id: 'gamut.forms.taskRefId' })}
          {intl.formatMessage({ id: 'gamut.noValueIndicatorColon' })}
          {taskRefId}
        </Typography>
      </Grid>

      <Grid item xs={6} sm={3} md={2} lg={2} xl={2} className={classes.inboxItemTitle}>
        <Typography component='span'>{title}</Typography>
      </Grid>

      <Grid item xs={12} sm={5} md={6} lg sx={{ flexGrow: 1 }} className={classes.inboxItemAttachments}>
        {props.children}
      </Grid>


      <Grid item xs={12} sm={2} md={2} lg sx={{ flexGrow: 0, flexShrink: 0, flexBasis: 'auto' }} className={classes.inboxItemSentAt}>
        <Typography variant='caption' sx={{ whiteSpace: 'nowrap' }}>
          {intl.formatMessage({ id: 'gamut.forms.lastModified' })}
        </Typography>
        <GDate variant='date-only' date={sentAt} />
      </Grid>

    </div>

  )
}





