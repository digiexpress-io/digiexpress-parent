import React from 'react'
import { OpenStreetMapProvider } from 'leaflet-geosearch'

import { MapApi } from './map-types'
import { _findAll } from './_findAll';
import { _getOne } from './_getOne';



export interface MapContextType {
//  provider: OpenStreetMapProvider;

  findAll(searchKeyword: string): Promise<MapApi.GeoLocation[]>;
  getOne(lat: number, lng: number): Promise<MapApi.GeoLocation>;
}

export const MapContext = React.createContext<MapContextType>({} as any);

export interface MapProviderProps {
  children: React.ReactNode;
  options: {
    countrycodes: string;
    locale: string;
 //   start: { lat: string, lon: string }
  },
}


const viewbox = '-180,-90,180,90'

export const MapProvider: React.FC<MapProviderProps> = (props) => {
  const { countrycodes, locale } = props.options;


  // root provider from leaflet, handles searches and svg
  const provider = React.useMemo(() => new OpenStreetMapProvider({
    params: {
      countrycodes,
      viewbox,
      addressdetails: 1,
      'accept-language': locale,
    }
  }), [countrycodes, locale, viewbox]);

  const findAll: (addressKeyword: string) => Promise<MapApi.GeoLocation[]> = React.useCallback((addressKeyword: string) => _findAll(addressKeyword, provider), [provider]);
  const getOne: (lat: number, lon: number) => Promise<MapApi.GeoLocation> = React.useCallback((lat: number, lon: number) => _getOne(lat, lon, locale), [locale]);

  const contextValue: MapContextType = React.useMemo(() => Object.freeze({ findAll, getOne }), [findAll, getOne]);
  return (<MapContext.Provider value={contextValue}>{props.children}</MapContext.Provider>);
}


export const useMap = () => {
  return React.useContext(MapContext);
}





