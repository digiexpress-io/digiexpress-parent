import React from 'react';
import { Backend } from '@taskclient';

function sleep(ms: number) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

const LoadingFC: React.FC<{ client: Backend }> = ({ client }) => {
  return <>...Loading: {client.config.url}</>
}

const DownFC: React.FC<{ client: Backend }> = ({ client }) => {
  return <>...Backend is not responding: {client.config.url}</>
}

const MisconfiguredFC: React.FC<{ client: Backend }> = ({ client }) => {
  return <>...Backend is found but getting 404: {client.config.url}</>
}

namespace Connection {
  export const Loading = LoadingFC;
  export const Down = DownFC;
  export const Misconfigured = MisconfiguredFC;
}

export default Connection;
