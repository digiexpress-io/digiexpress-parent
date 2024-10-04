import { DateTime } from "luxon";
import { ContractApi } from '../api-contract';


import {
  mapToBooking
} from './mappers'



export namespace BookingApi {
  export const mapper = mapToBooking;
}

export declare namespace BookingApi {
  export type BookingId = string;

  export interface Booking {
    id: BookingId;
    scheduledAt: DateTime;
    contractId: ContractApi.ContractId;
  }

  export type CancelBookingFetchPOST = (request: Booking) => Promise<Response>;
  export type GetBookingsFetchGET = () => Promise<Response>;

  export interface BookingContextType {
    bookings: readonly Booking[];
    bookingStats: { total: number },

    cancelBooking: (request: Booking) => Promise<Booking>;
    getBooking(offerId: BookingId): Booking | undefined;
    refresh(): Promise<void>;
    isPending: boolean;
  }

}