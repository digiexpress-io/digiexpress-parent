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
    <div className={classes.inboxItem} onClick={() => onClick(id)}>
      <Grid item xs={6} sm={2} md={2} lg={2} xl={2} className={classes.taskRefLayout}>

        {isViewed ? <></> : <Tooltip title={tooltipContent}>
          <MarkEmailUnreadOutlinedIcon className={classes.newMsgIndicator} />
        </Tooltip>}
        <Typography variant='caption'>
          {intl.formatMessage({ id: 'gamut.forms.taskRefId' })}
          {intl.formatMessage({ id: 'gamut.noValueIndicatorColon' })}
          {taskRefId}
        </Typography>
      </Grid>

      <Grid item xs={6} sm={3} md={2} lg={6} xl={6} className={classes.inboxItemTitle}>
        <Typography component='span'>{title}</Typography>
      </Grid>

      <Grid item md={4} lg={4} xl={4} className={classes.inboxItemAttachments}>
        {props.children}
      </Grid>

      <Grid item className={classes.inboxItemSentAt}>
        <GDate variant='date-only' date={sentAt} />
      </Grid>

    </div>

  )
}





