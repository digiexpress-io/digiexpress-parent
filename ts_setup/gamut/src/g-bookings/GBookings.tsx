import React from 'react';
import { Grid, Typography, useThemeProps } from '@mui/material';
import { useIntl } from 'react-intl';
import { GFlex, useBookings } from '../';
import { GBookingsRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { GBookingItemProps, GBookingItem } from './GBookingItem';
import { GOverridableComponent } from '../g-override';



export interface GBookingsProps {
  slots?: {
    item?: React.ElementType<GBookingItemProps>
  };
  slotProps?: {
    item: {
      color: string;
      onClick: () => void;
    }
  },
  component?: GOverridableComponent<GBookingsProps>
}


export const GBookings: React.FC<GBookingsProps> = (initProps) => {
  const intl = useIntl();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME
  });

  const classes = useUtilityClasses();
  const { bookings } = useBookings();
  const { slots, slotProps, component } = props;

  const Item: React.ElementType<GBookingItemProps> = slots?.item ?? GBookingItem;
  const Root = component ?? GBookingsRoot;

  return (
    <Root className={classes.root} ownerState={props}>
      <GFlex variant='header'>
        <Grid container>
          <Grid item lg={6}><Typography className={classes.header}>{intl.formatMessage({ id: 'gamut.bookings.serviceName' })}</Typography></Grid>
          <Grid item lg={4}><Typography className={classes.header}>{intl.formatMessage({ id: 'gamut.bookings.bookingDateTime' })}</Typography></Grid>
        </Grid>
      </GFlex>
      {bookings.map((booking) => (
        <Item
          key={booking.id}
          color={slotProps?.item.color}
          name={booking.contractId}
          scheduledAt={booking.scheduledAt}
          onClick={slotProps?.item.onClick}
        />
      ))}
    </Root>)
}


