import React from 'react';
import { Box, Divider, Grid, Tooltip, Typography, useThemeProps } from '@mui/material';
import EmailOutlinedIcon from '@mui/icons-material/EmailOutlined';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { GDate } from '../g-date';
import { GInboxItemRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
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
  const { title, subTitle, senderName, sentAt, onClick, id, contractStatus, taskRefId } = props;

  const tooltipContent = subTitle ? senderName + "said: " + props.subTitle : 'no messages';
  const subject = getSubject(id);
  const isViewed = subject?.isViewed;


  return (
    <GInboxItemRoot container className={classes.itemRoot} onClick={() => onClick(id)}>

      <Grid item xs={6} sm={2} md={2} lg={2} xl={2}>
        {isViewed ? <></> : <EmailOutlinedIcon />}
        <Typography variant='caption'>
          {intl.formatMessage({ id: 'gamut.forms.taskRefId' })}
          {intl.formatMessage({ id: 'gamut.noValueIndicatorColon' })}
          {taskRefId}
        </Typography>
      </Grid>

      <Grid item xs={6} sm={3} md={2} lg={2} xl={2} className={classes.itemTitle}>
        <Tooltip title={tooltipContent}>
          <Typography component='span'>{title}</Typography>
        </Tooltip>
      </Grid>

      <Grid item xs={12} sm={5} md={6} lg={7} xl={7} className={classes.itemAttachments}>
        {props.children}
      </Grid>


      <Grid item xs={12} sm={2} md={2} lg={1} xl={1} className={classes.itemSentAt}>
        <Typography variant='caption'>last modified</Typography>
        <GDate variant='date-only' date={sentAt} />
      </Grid>

    </GInboxItemRoot>

  )
}





