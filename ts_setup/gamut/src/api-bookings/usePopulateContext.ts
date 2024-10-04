import React from 'react';

import { useQuery } from '@tanstack/react-query'

import { BookingApi } from './booking-types';
import { mapToBooking, mapToBookingData } from './mappers';



export interface UsePropulateProps {
  options: { staleTime: number, queryKey: string };
  cancelBooking: BookingApi.CancelBookingFetchPOST;
  getBookings: BookingApi.GetBookingsFetchGET;
}

export interface PopulateBookingContext {
  bookings: readonly BookingApi.Booking[];
  isPending: boolean;
  cancelBooking: (request: BookingApi.Booking) => Promise<BookingApi.Booking>;
  refresh(): Promise<void>;
}

export function usePopulateContext(props: UsePropulateProps): PopulateBookingContext {
  const [isInitialLoadDone, setInitialLoadDone] = React.useState(false);
  const { getBookings, options } = props;
  const { staleTime, queryKey } = options;

  // tanstack query config
  const { data: bookings, error, refetch, isPending } = useQuery({
    staleTime,
    queryKey: [queryKey],
    queryFn: () => getBookings()
      .then(data => data.json())
      .then((data: BookingApi.Booking[]) => {
        return data;
      }),
  });



  // Create new booking and reload after that
  const cancelBooking: (request: BookingApi.Booking) => Promise<BookingApi.Booking> = React.useCallback(async (request) => {
    const newBooking: BookingApi.Booking = await props.cancelBooking(request).then(resp => resp.json()).then(mapToBooking);
    return refetch().then(() => newBooking);
  }, [refetch, props.cancelBooking]);



  // Reload all data
  const refresh: () => Promise<void> = React.useCallback(async () => {
    return refetch().then(() => { });
  }, [refetch]);

  // track initial loading
  React.useEffect(() => {
    if (isInitialLoadDone) {
      return;
    }
    if (bookings) {
      setInitialLoadDone(true);
    }
  }, [isInitialLoadDone, bookings]);

  const isContextLoaded = (isInitialLoadDone || !isPending);
  const bookingData = mapToBookingData(bookings ?? []);

  // cache the end result
  return React.useMemo(() => {
    return { bookings: bookingData?.bookings ?? [], isPending: !isContextLoaded, cancelBooking, refresh };
  }, [bookingData?.hash, isContextLoaded, cancelBooking, refresh]);
}
