import { Md5 } from 'ts-md5';
import { BookingApi } from './booking-types';
import { DateTime } from 'luxon';


export function mapToBooking(data: BookingApi.Booking): BookingApi.Booking {
  return {
    id: data.id,
    contractId: data.contractId,
    scheduledAt: DateTime.fromISO(data.scheduledAt + '')
  };
}

export function mapToBookingData(data: BookingApi.Booking[]): {
  hash: string;
  bookings: readonly BookingApi.Booking[];
} {
  const md5 = new Md5();
  const bookings: BookingApi.Booking[] = [];

  if (!Array.isArray(data)) {
    throw new TypeError('Expected data to be an array');
  }

  for (const proc of data) {
    const booking = mapToBooking(proc);
    md5
      .appendStr(proc.id)
      .appendStr(proc.contractId)
      .appendStr(DateTime.fromISO(proc.scheduledAt + '').toMillis() + '');

    bookings.push(booking);
  }

  return { bookings: Object.freeze(bookings), hash: md5.end() + '' };
}
