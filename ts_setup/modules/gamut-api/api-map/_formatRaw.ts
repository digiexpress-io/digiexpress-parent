import { MapApi } from "./map-types";


/*
 * Combining address fields with non-empty values into one address line. 
 * field list from https://nominatim.org/release-docs/develop/api/Output/
 */
export function _formatRaw(address: MapApi.RawAddress | undefined): string | undefined {
  if (address) {
    const fragment_1 = `${address.road || ''} ${address.house_number || address.house_name || ''}`;
    const fragment_2 = (
      address.city_district ??
      address.district ??
      address.borough ??
      address.suburb ??
      address.subdivision ??
      address.hamlet ??
      address.croft ??
      address.isolated_dwelling,
      address.city ??
      address.town ??
      address.village ??
      address.municipality);

    // European format, postal code after street address
    // English has postal code last
    // TODO: make it customizable
    return [fragment_1, address.postcode, fragment_2]
      .map(f => f?.trim())
      .filter(f => !!f)
      .join(", ");
  }
  return undefined;
}