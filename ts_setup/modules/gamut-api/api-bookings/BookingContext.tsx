import React from 'react';
import { BookingApi } from './booking-types';
import { usePopulateContext } from './usePopulateContext';



export const BookingContext = React.createContext<BookingApi.BookingContextType>({} as any);

export const BookingProvider: React.FC<{
  children: React.ReactNode;
  options: { staleTime: number, queryKey: string };
  cancelBooking: BookingApi.CancelBookingFetchPOST;
  getBookings: BookingApi.GetBookingsFetchGET;
}> = (props) => {

  const data = usePopulateContext(props);

  return React.useMemo(() => {
    const contextValue: BookingApi.BookingContextType = {
      bookings: data.bookings,
      bookingStats: Object.freeze({ total: data.bookings.length }),
      isPending: data.isPending,
      cancelBooking: data.cancelBooking,
      getBooking: (id) => data.bookings.find((booking) => booking.id === id),
      refresh: data.refresh,
    };

    return (<BookingContext.Provider value={contextValue}>{props.children}</BookingContext.Provider>);
  }, [data, props]);
}

export function useBookings() {
  const result: BookingApi.BookingContextType = React.useContext(BookingContext);
  return result;
}