import React from 'react';
import { ThemeProvider, StyledEngineProvider, CircularProgress, LinearProgress } from '@mui/material';
import Client from '../core';


function sleep(ms: number) {
    return new Promise( resolve => setTimeout(resolve, ms) );
}

const LoadingFC: React.FC<{client: Client.Service}> = ({client}) => {
  return <>...Loading: {client.config.url}</>
}

const DownFC: React.FC<{client: Client.Service}> = ({client}) => {
  return <>...Backend is not responding: {client.config.url}</>
}

const MisconfiguredFC: React.FC<{client: Client.Service}> = ({client}) => {
  return <>...Backend is found but getting 404: {client.config.url}</>
}

namespace Connection {
  export const Loading = LoadingFC;
  export const Down = DownFC;
  export const Misconfigured = MisconfiguredFC;
}

export default Connection;
