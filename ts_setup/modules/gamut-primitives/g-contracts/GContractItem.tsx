import React from 'react';
import { Avatar, Box, Grid, Typography, useThemeProps, Tooltip } from '@mui/material';
import { MarkEmailUnreadOutlined as MarkEmailUnreadOutlinedIcon } from '@mui/icons-material';
import { TransferWithinAStation as TransferWithinAStationIcon } from '@mui/icons-material';

import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { GFlex } from '../g-flex';
import { GDate, GDateProps } from '../g-date';
import { GContractItem as ContractItem, useUtilityClasses, MUI_NAME } from './useUtilityClasses';

export interface GContractItemProps {
  exchangeId: string;
  referenceId: string;
  name: string;
  status: string;
  assigned: boolean | undefined;
  lastModified: DateTime;
  documents?: number | undefined;
  messages?: number | undefined;
  hasUnviewedMessages?: boolean;
  onClick: (exchangeId: string) => void;
  date?: Partial<GDateProps>;
  slotProps?: {
    date?: Partial<GDateProps>;
  };
}

export const GContractItem: React.FC<GContractItemProps> = (initProps) => {
  const intl = useIntl();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();
  const { lastModified, name, status, assigned, documents, messages, hasUnviewedMessages, onClick, slotProps = {}, exchangeId, referenceId } = props;

  const ownerState = {
    ...props,
    dateVariant: slotProps.date?.variant ?? 'date-only'
  }

  const statusLabel = (() => {
    switch (status) {
      case 'OPEN':
        return intl.formatMessage({ id: 'gamut.forms.status.OPEN' });
      case 'NEW':
        return intl.formatMessage({ id: 'gamut.forms.status.NEW' });
      case 'COMPLETED':
        return intl.formatMessage({ id: 'gamut.forms.status.COMPLETED' });
      case 'REJECTED':
        return intl.formatMessage({ id: 'gamut.forms.status.REJECTED' });
      case 'DELEGATED':
        return intl.formatMessage({ id: 'gamut.forms.status.DELEGATED' });
      case 'TRANSFERRED':
        return intl.formatMessage({ id: 'gamut.forms.status.TRANSFERRED' });
      case 'WAITING':
        return intl.formatMessage({ id: 'gamut.forms.status.WAITING' });
      default:
        return status;
    }
  })();

  return (
    <ContractItem className={classes.contractItem} ownerState={ownerState} onClick={() => onClick(exchangeId)}>
      <GFlex variant='body'>
        <Box className={assigned ? classes.assigned : undefined}>
        <Grid container>
            {assigned ? (
              <Grid item xs={12} sm={12} md={12} lg={3} xl={3}>
                <Box display='flex' flexDirection='column'>
                  <Typography>{name}</Typography>
                  <Box className={classes.assignedIndicator}>
                    <TransferWithinAStationIcon />
                    <Typography variant='subtitle1'>{intl.formatMessage({ id: 'gamut.offers.assignedToMe', defaultMessage: 'Assigned to me:' })} 1/3 completed</Typography>
                  </Box>
                </Box>
              </Grid>
            ) : (
                <Grid item xs={12} sm={12} md={12} lg={3} xl={3}>
                  <Typography>{name}</Typography>
                </Grid>
            )
            }
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
              {statusLabel}
            </Typography>
          </Grid>

            <Grid item xs={12} sm={12} md={12} lg={1} xl={1} display='flex' alignItems='center'>
            <GFlex variant='hidden' hiddenOn={(br) => br.up('lg')}>
              <Typography component='span' className={classes.files}>
                {intl.formatMessage({ id: 'gamut.forms.files' })}
              </Typography>
            </GFlex>
            {documents ? (
              <Avatar className={classes.filesCount}>
                <Typography>{documents}</Typography>
              </Avatar>
            ) : (
              <Avatar className={classes.noValue}>{intl.formatMessage({ id: 'gamut.noValueIndicator' })}</Avatar>
            )}
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={1} xl={1}>
            <GFlex variant='hidden' hiddenOn={(br) => br.up('lg')}>
              <Typography component='span' className={classes.messages}>
                {intl.formatMessage({ id: 'gamut.forms.messages' })}
              </Typography>
            </GFlex>
            <Box sx={{ position: 'relative', display: 'inline-flex' }}>
              <Avatar className={messages ? classes.messagesCount : classes.noValue}>
                <Typography>{messages ?? intl.formatMessage({ id: 'gamut.noValueIndicator' })}</Typography>
              </Avatar>
              {hasUnviewedMessages && (
                <Tooltip title={intl.formatMessage({ id: 'cust.inbox.message.sender-name.org-user' })}>
                  <Box
                    sx={{
                      position: 'absolute',
                      top: -6,
                      right: -6,
                    }}
                  >
                    <MarkEmailUnreadOutlinedIcon
                      className={classes.newMsgIndicator}
                      fontSize="small"
                    />
                  </Box>
                </Tooltip>
              )}
            </Box>

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
        </Box>

      </GFlex>
    </ContractItem>
  );
};
