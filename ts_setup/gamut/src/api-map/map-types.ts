export namespace MapApi {

}

export declare namespace MapApi {

  export interface GeoLocation {
    lat: number;
    lon: number;
    formattedAddress: string;
    rawAddress: RawAddress | undefined;
  }


  export interface RawAddress {


    road: string | undefined;
    house_number: string | undefined;
    house_name: string | undefined;
    city_district: string | undefined;
    district: string | undefined;
    borough: string | undefined;
    suburb: string | undefined;
    subdivision: string | undefined;
    hamlet: string | undefined;
    croft: string | undefined;
    isolated_dwelling: string | undefined;
    city: string | undefined;
    town: string | undefined;
    village: string | undefined;
    municipality: string | undefined;
    postcode: string | undefined;
  }

  export interface RawResult {
    place_id: string;
    licence: string;
    osm_type: string;
    osm_id: number;
    boundingbox: [string, string, string, string];
    lat: string;
    lon: string;

    display_name: string;
    class: string;
    type: string;
    importance: number;
    icon?: string;
    address?: RawAddress | undefined;
  }
  export type PointTuple = [number, number];
  export type BoundsTuple = [PointTuple, PointTuple];

  export interface SearchResult {
    x: number;
    y: number;
    label: string;
    bounds: BoundsTuple | null;
    raw: RawResult;
  }
}
