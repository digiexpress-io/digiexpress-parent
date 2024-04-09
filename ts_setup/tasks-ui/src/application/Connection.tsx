import React from 'react';
import { Backend } from 'descriptor-backend';

function sleep(ms: number) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

const LoadingFC: React.FC<{ client: Backend }> = ({ client }) => {
  return <>...Loading: {JSON.stringify(client.config.urls)}</>
}

const DownFC: React.FC<{ client: Backend }> = ({ client }) => {
  return <>...Backend is not responding: {JSON.stringify(client.config.urls)}</>
}

const MisconfiguredFC: React.FC<{ client: Backend }> = ({ client }) => {
  return <>...Backend is found but getting 404: {JSON.stringify(client.config.urls)}</>
}

namespace Connection {
  export const Loading = LoadingFC;
  export const Down = DownFC;
  export const Misconfigured = MisconfiguredFC;
}

export default Connection;
