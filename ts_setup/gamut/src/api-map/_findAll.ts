
import { OpenStreetMapProvider } from 'leaflet-geosearch';
import { MapApi } from './map-types';
import { _formatRaw } from './_formatRaw';



export async function _findAll(addressKeyword: string, provider: OpenStreetMapProvider): Promise<MapApi.GeoLocation[]> {
  const searchResults: MapApi.SearchResult[] = await provider.search({ query: addressKeyword });


  const mapped = searchResults.map((result: MapApi.SearchResult) => {
    const lat: number = Number(result.raw.lat);
    const lon: number = Number(result.raw.lon);
    
    const rawAddress: MapApi.RawAddress | undefined = result.raw.address;
    const defaultVal = result.label;
    const formattedAddress: string = _formatRaw(rawAddress) ?? defaultVal;

    return Object.freeze({ lat, lon, rawAddress, formattedAddress })
  });
  
  return mapped.filter(e => e.formattedAddress);
}
