import React from 'react'

import { Box } from '@mui/material'

import { LatLngLiteral, Marker } from 'leaflet'
import * as Leaflet from 'react-leaflet'

import { GInputBaseAnyProps } from '../g-input-base'
import { GInputAddressProps } from './GInputAddress'
import { useMap } from '../api-map'
import { useInput } from './InputProvider'





export const InputMap: React.FC<GInputBaseAnyProps & GInputAddressProps> = ({ options, value: backendValue }) => {
  
  const input = useInput();
  const { getOne, findAll } = useMap();
  const markerRef = React.useRef<Marker>(null);
  const [position, setPosition] = React.useState<LatLngLiteral>({ ...(options?.defaultValue ?? { lat: 0, lng: 0 }) });


  function dragend() {
    const marker = markerRef.current;
    if (marker != null) {
      const pos = marker.getLatLng();
      getOne(pos.lat, pos.lng).then(address => {
        setPosition(pos);
        input.setValue(address.formattedAddress);
      });
    }
  }

  React.useEffect(() => {
    if (backendValue) {
      findAll(backendValue).then((found) => {
        const [first] = found;
        if (first) {
          setPosition({ lat: first.lat, lng: first.lon });
        }
      });
    }
  }, [backendValue]);

  return (
    <Resizer>
      {(dimensions) => (
      <Leaflet.MapContainer center={position} zoom={13} scrollWheelZoom={true} style={{ height: '500px' }}>
        <Leaflet.TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        <Center position={position} />

        <Leaflet.Marker draggable position={position} ref={markerRef} eventHandlers={{ dragend }}>
          <Leaflet.Popup minWidth={dimensions.width}></Leaflet.Popup>
        </Leaflet.Marker>
      </Leaflet.MapContainer>)}
    </Resizer>
  );
}


const Center: React.FC<{ position: LatLngLiteral }> = ({ position }) => {
  const map = Leaflet.useMap();
  React.useEffect(() => {
    map.setView(position, map.getZoom());
  }, [position])

  return (<></>);
}






const Resizer: React.FC<{ children: (props: { width: number}) => React.ReactNode}> = ({ children }) => {
  const ref = React.useRef<HTMLDivElement>(null);
  const [dimensions, setDimensions] = React.useState({ width: 100, height: 0 });

  React.useEffect(() => {
    function getDimensions() {
      return {
        width: ref.current?.offsetWidth ?? 100,
        height: ref.current?.offsetHeight ?? 0
      };
    }

    const handleResize = () => {
      setDimensions(getDimensions())
    }

    if (ref.current) {
      setDimensions(getDimensions())
    }

    window.addEventListener("resize", handleResize)

    return () => {
      window.removeEventListener("resize", handleResize)
    }
  }, [ref])



  return (
    <Box sx={{ width: '100%'  }} ref={ref}>
      {children(dimensions)}
    </Box>
  );
}



