import { BookingApi } from "@dxs-ts/gamut";
import { DateTime } from 'luxon';



export function createBookingFetch(url: (string | undefined) = 'http://localhost:8080/portal/secured/bookings') {
  const fetchPost: BookingApi.CancelBookingFetchPOST = async (request: BookingApi.Booking) => {
    const response = await window.fetch(url + "?id=" + request.id, {
      method: 'POST',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchGet: BookingApi.GetBookingsFetchGET = async () => {
    /*const response = await window.fetch(url, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });*/

    const mockBookingItems: BookingApi.Booking[] = [
      {
        id: 'booking1',
        contractId: 'School counselor consultation',
        scheduledAt: DateTime.fromObject({
          year: 2025, month: 9, day: 29,
          hour: 10, minute: 30
        }),
      },
      {
        id: 'booking2',
        contractId: 'Building permit inspection',
        scheduledAt: DateTime.fromObject({
          year: 2025, month: 10, day: 12,
          hour: 13, minute: 15
        }),

      },
      {
        id: 'booking3',
        contractId: 'Building permit inspection',
        scheduledAt: DateTime.fromObject({
          year: 2025, month: 10, day: 12,
          hour: 13, minute: 15
        }),

      },
    ];
    const mockResponse = new Response(JSON.stringify(mockBookingItems), {
      status: 200,
      headers: { 'Content-Type': 'application/json' }
    });

    return mockResponse;
  };



  return { fetchGet, fetchPost };
}
