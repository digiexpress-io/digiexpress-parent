import { _formatRaw } from './_formatRaw';
import { MapApi } from './map-types';

export async function _getOne(lat: number, lon: number, acceptLanguage: string): Promise<MapApi.GeoLocation> {
  try {
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}&zoom=18&addressdetails=1&accept-language=${acceptLanguage}`;
    const searchResult = await fetch(url)
      .then(response => {
        if (!response.ok) {
          throw new Error("Failed to resolve address, HTTP status code: " + response.status);
        }
        return response.json();
      });

    const rawAddress = searchResult.address;
    const formattedAddress = _formatRaw(rawAddress) ?? searchResult.display_name ?? '';
    return { formattedAddress, rawAddress, lat, lon };
  } catch (e) {
    console.log("Failed to resolve address, error in reverse search: " + e);
    return { formattedAddress: '', rawAddress: undefined, lat, lon };
  }
}