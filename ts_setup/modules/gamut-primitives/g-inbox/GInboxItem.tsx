import React from 'react';
import { Tooltip, Typography, useThemeProps, Grid2 } from '@mui/material';
import MarkEmailUnreadOutlinedIcon from '@mui/icons-material/MarkEmailUnreadOutlined';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { GDate } from '../g-date';
import { MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { useComms } from '@dxs-ts/gamut-api';


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
  const { getSubject, markViewed } = useComms();

  const handleClick = () => {
    markViewed(id);
    onClick(id);
  };

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { title, subTitle, senderName, sentAt, onClick, id, taskRefId } = props;

  const tooltipContent = subTitle && intl.formatMessage({ id: 'gamut.inbox.newMessageFrom' }) + senderName + ": " + props.subTitle;
  const subject = getSubject(id);

  const isViewed = subject?.isViewed;

  return (
    <Grid2 container className={classes.inboxItem} onClick={handleClick} >
      <Grid2 size={{ xs: 12, sm: 12, md: 3, lg: 2 }} className={classes.taskRefLayout}>
        {!isViewed && (
          <Tooltip title={tooltipContent}>
            <MarkEmailUnreadOutlinedIcon className={classes.newMsgIndicator} />
          </Tooltip>
        )}
        <Typography variant='caption'>
          {intl.formatMessage({ id: 'gamut.forms.taskRefId' })}
          {intl.formatMessage({ id: 'gamut.noValueIndicatorColon' })}
          {taskRefId}
        </Typography>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 3, lg: 3 }} className={classes.inboxItemTitle}>
        <Typography component='span'>{title}</Typography>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 6, lg: 6 }} className={classes.inboxItemAttachments}>
        {props.children}
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 1, lg: 1 }} className={classes.inboxItemSentAt}>
        <GDate variant='date-only' date={sentAt} />
      </Grid2>
    </Grid2>
  );
}





