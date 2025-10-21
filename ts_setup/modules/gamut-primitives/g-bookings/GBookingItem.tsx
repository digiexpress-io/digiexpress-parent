import React from 'react';
import { Button, Grid, Typography, useThemeProps } from '@mui/material';
import { DeleteForever as DeleteForeverIcon } from '@mui/icons-material';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { GBookingItem as ItemRoot, useUtilityClasses, MUI_NAME } from './useUtilityClasses';
import { GConfirm } from '../g-confirm'
import { GDate, GDateProps } from '../g-date';
import { GFlex } from '../g-flex';

export interface GBookingItemProps {
  name: string;
  scheduledAt: DateTime;
  onClick?: () => void;

  slotProps?: {
    date?: Partial<GDateProps>
  },
  component?: React.ElementType<GBookingItemProps>;

}

export const GBookingItem: React.FC<GBookingItemProps> = (initProps) => {
  const intl = useIntl();
  const [confirmOpen, setConfirmOpen] = React.useState(false);

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();
  const { scheduledAt, name, slotProps = {}, onClick } = props;
  const ownerState = {
    ...props,
    dateVariant: slotProps.date?.variant ?? 'date-only'
  }


  function handleCancelConfirm() {
    setConfirmOpen(prev => !prev)
  }

  return (<>
    <GConfirm
      open={confirmOpen}
      onClose={handleCancelConfirm}
      cancelItemName={props.name}
      cancelItemMeta={<>
        {intl.formatMessage({ id: 'gamut.bookings.bookingDateTime' })}
        {intl.formatMessage({ id: 'gamut.textSeparator', defaultMessage: ' ' })}
        <GDate variant='date-only' date={props.scheduledAt} />
      </>
      }
      title={intl.formatMessage({ id: 'gamut.bookings.deleteBooking.title' })}
      content={intl.formatMessage({ id: 'gamut.bookings.deleteBooking.content' })}
      cancelButton={intl.formatMessage({ id: 'gamut.bookings.deleteBooking.cancelButton' })}
      deleteButton={intl.formatMessage({ id: 'gamut.bookings.deleteBooking.deleteButton' })}
    />
    <ItemRoot className={classes.root} ownerState={ownerState} as={ownerState.component} onClick={onClick}>
      <GFlex variant='body'>
        <Grid container>
          <Grid item xs={12} sm={12} md={12} lg={6} xl={6} >
            <Typography>{name}</Typography>
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={4} xl={4}>
            <GFlex variant='hidden' hiddenOn={(br) => br.up('lg')}>
              <Typography component='span' className={classes.started}>
                {intl.formatMessage({ id: 'gamut.bookings.bookingDateTime' })}
              </Typography>
            </GFlex>
            <Typography component='span'>
              <GDate variant={ownerState.dateVariant} date={scheduledAt} />
            </Typography>
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={2} xl={2}>
            <Button startIcon={<DeleteForeverIcon />} className={classes.cancel} onClick={handleCancelConfirm}>
              <Typography>{intl.formatMessage({ id: 'gamut.buttons.cancel' })}</Typography>
            </Button>
          </Grid>
        </Grid>
      </GFlex>
    </ItemRoot>
  </>
  )
}
