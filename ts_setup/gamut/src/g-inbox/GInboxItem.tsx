import React from 'react';
import { Grid, Typography, useThemeProps } from '@mui/material';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { GDate } from '../g-date';
import { GInboxItemRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';


export interface GInboxItemProps {
  children: React.ReactNode
  id: string;
  title: string;
  subTitle: string;
  senderName: string;
  sentAt: DateTime;
  contractStatus: string;
  onClick: (subjectId: string) => void;
}

export const GInboxItem: React.FC<GInboxItemProps> = (initProps) => {
  const intl = useIntl();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { title, subTitle, senderName, sentAt, onClick, id, contractStatus } = props;
  const classes = useUtilityClasses();

  return (
    <GInboxItemRoot container className={classes.itemRoot} onClick={() => onClick(id)} >
      <Grid item xs={12} sm={12} md={12} lg={2} xl={2}>
        <Typography>
          {senderName}
        </Typography>
      </Grid>

      <Grid item xs={12} sm={12} md={12} lg={8} xl={8}>
        <div className={classes.itemText}>
          <Typography component='span' className={classes.itemTitle}>{title}{intl.formatMessage({ id: 'gamut.noValueIndicator' })}</Typography>
          <Typography component='span' className={classes.itemSubTitle}>{subTitle}</Typography>
        </div>
      </Grid>

      <Grid item xs={12} sm={12} md={12} lg={2} xl={2} className={classes.itemSentAt}>
        <Typography>
          <GDate variant='relative' date={sentAt} />
        </Typography>
      </Grid>

      <Grid item className={classes.itemLayout}>
        {props.children}
      </Grid>

      <Grid item xs={12} sm={12} md={12} lg={12} xl={12} className={classes.itemSentAt}>
        <Typography>
          {contractStatus}
        </Typography>
      </Grid>

    </GInboxItemRoot>

  )
}





