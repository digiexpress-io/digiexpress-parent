import { createFileRoute, redirect, Navigate } from '@tanstack/react-router'
import { useIam } from '../api-iam';
import React from 'react';

export const Route = createFileRoute('/')({
  component: Component,

})


function Component() {
  const iam = useIam();
  if(iam.authType === 'ANON') {
    return <Navigate {...{
      from: '/',
      to: '/public/$locale',
      params: { viewId: '', locale: 'en' }
    }}/>
  } else {
    return <Navigate {...{
      from: '/',
      to: '/secured/$locale/views/$viewId',
      params: { viewId: 'user-overview', locale: 'en' }
    }}/>
  }
}