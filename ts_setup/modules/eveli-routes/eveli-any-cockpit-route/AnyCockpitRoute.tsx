import React from 'react';
import { useNavigate } from '@tanstack/react-router'

import { CockpitsBackendProvider, CockpitsBackendProviderProps } from '@dxs-ts/cockpit-api';
import { useFetch } from '@dxs-ts/envir-fetch';



export const AnyCockpitRoute: React.FC<{children: React.ReactNode}> = ({ children }) => {
  const navigate = _useCockpitNavigate();
  const persistence = _useCockpitPersistence();

  return (
    <CockpitsBackendProvider navigate={navigate} persistence={persistence}>
      {children}
    </CockpitsBackendProvider>)
}

function _useCockpitNavigate(): CockpitsBackendProviderProps['navigate'] {
  const navigate = useNavigate();

  return {
    findAllCockpits: () => navigate({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/cockpits'
    }),
    createOneCockpit: () => {
      navigate({
        from: '/secured/$locale/worker',
        to: '/secured/$locale/worker/cockpits'
      });
    },
    getOneCockpit: (cockpitId: string) => navigate({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/cockpits/$cockpitId',
      params: { cockpitId }
    }),
  }
}

function _useCockpitPersistence(): CockpitsBackendProviderProps['persistence'] {
  const { findAllCockpits, findActivity } = useFetch('worker/rest/api/cockpits.GET', []);
  const { createCockpit } = useFetch('worker/rest/api/cockpits.POST', {});
  const { getOneCockpit } = useFetch('worker/rest/api/cockpits/$cockpitId.GET', {});
  const { createCockpitTenant } = useFetch('worker/rest/api/cockpits/$cockpitId/tenants.POST', {});

  const unit: CockpitsBackendProviderProps['persistence'] = {
    findAllCockpits: findAllCockpits,
    createOneCockpit: createCockpit,
    getOneCockpit: getOneCockpit,
    createOneCockpitTenant: createCockpitTenant,
    findActivity
  }

  return unit;
}