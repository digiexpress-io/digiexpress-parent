import React from 'react';
import { Avatar, Grid, Typography, useThemeProps } from '@mui/material';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { GFlex } from '../g-flex';
import { GDate, GDateProps } from '../g-date';
import { GContractItemRoot, useUtilityClasses, MUI_NAME } from './useUtilityClasses';
import { useContracts } from '../api-contract';


export interface GContractItemProps {
  exchangeId: string;
  referenceId: string;
  name: string;
  status: string;
  lastModified: DateTime;
  documents?: number | undefined;
  messages?: number | undefined;
  onClick: (exchangeId: string) => void;
  date?: Partial<GDateProps>

  slotProps?: {
    date?: Partial<GDateProps>
  },

}


export const GContractItem: React.FC<GContractItemProps> = (initProps) => {
  const intl = useIntl();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();
  const { lastModified, name, status, documents, messages, onClick, slotProps = {}, exchangeId, referenceId } = props;

  const ownerState = {
    ...props,
    dateVariant: slotProps.date?.variant ?? 'date-only'
  }

  return (
    <GContractItemRoot className={classes.item} ownerState={ownerState} onClick={() => onClick(exchangeId)}>
      <GFlex variant='body'>
        <Grid container>
          <Grid item xs={12} sm={12} md={12} lg={3} xl={3}>
            <Typography>{name}</Typography>
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={2} xl={2}>
            <GFlex variant='hidden' hiddenOn={(br) => br.up('lg')}>
              <Typography component='span' className={classes.taskRefId}>
                {intl.formatMessage({ id: 'gamut.forms.taskRefId' })}
              </Typography>
            </GFlex>
            <Typography component='span'>{referenceId}</Typography>
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={2} xl={2}>
            <GFlex variant='hidden' hiddenOn={(br) => br.up('lg')}>
              <Typography component='span' className={classes.status}>
                {intl.formatMessage({ id: 'gamut.forms.status' })}
              </Typography>
            </GFlex>
            <Typography component='span'>
              {intl.formatMessage({ id: `gamut.forms.status.${status}` })}
            </Typography>
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={1} xl={1}>
            <GFlex variant='hidden' hiddenOn={(br) => br.up('lg')}>
              <Typography component='span' className={classes.files}>
                {intl.formatMessage({ id: 'gamut.forms.files' })}
              </Typography>
            </GFlex>
            {documents ? <Avatar className={classes.filesCount}>
              <Typography>{documents}</Typography>
            </Avatar>
              :
              <Avatar className={classes.noValue}>{intl.formatMessage({ id: 'gamut.noValueIndicator' })}</Avatar>}
          </Grid>


          <Grid item xs={12} sm={12} md={12} lg={1} xl={1}>
            <GFlex variant='hidden' hiddenOn={(br) => br.up('lg')}>
              <Typography component='span' className={classes.messages}>
                {intl.formatMessage({ id: 'gamut.forms.messages' })}
              </Typography>
            </GFlex>
            {messages ? <Avatar className={classes.messagesCount}>
              <Typography>{messages}</Typography>
            </Avatar>
              :
              <Avatar className={classes.noValue}>{intl.formatMessage({ id: 'gamut.noValueIndicator' })}</Avatar>}
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={3} xl={3}>
            <GFlex variant='hidden' hiddenOn={(br) => br.up('lg')}>
              <Typography component='span' className={classes.lastModified}>
                {intl.formatMessage({ id: 'gamut.forms.lastModified' })}
              </Typography>
            </GFlex>
            <Typography component='span'>
              <GDate variant={ownerState.dateVariant} date={lastModified} />
            </Typography>
          </Grid>

          {/* Dummy item to compensate for GFlexBody css .MuiGrid-item:last-of-type */}
          <Grid item xs={12} sm={12} md={12} lg={1} xl={1} />
        </Grid>
      </GFlex>
    </GContractItemRoot>
  )
}
