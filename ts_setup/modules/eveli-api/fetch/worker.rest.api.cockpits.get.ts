import { createFileFetch } from '@dxs-ts/envir-fetch';
import { CockpitApi } from '@dxs-ts/cockpit-api';

export const Hook = createFileFetch('worker/rest/api/cockpits.GET')({
  hook
})

function hook(_props: {}) {
  const params = Hook.useParams();
  const { url } = params;
  
  return {
    findAllCockpits: async (): Promise<CockpitApi.CockpitSummary[]> => {
      return params.fetch(`${url({})}`)
        .then(response => response.json())
        .then((data: CockpitApi.CockpitContainer[]) => {
          return data.map((container: CockpitApi.CockpitContainer) => ({
            id: container.config.id,
            name: container.config.cockpitConfigName,
            description: container.config.cockpitConfigDesc,
            src: container
          }));
        })
    },
    findActivity: async (): Promise<CockpitApi.CockpitActivity> => {
      return params.fetch(`${url({})}/activity`)
        .then(response => response.json())
        .then(_mapToCockpitActivity)
    }
  }
}


function _mapToCockpitActivity(activityArray: CockpitActivityTypes[]): CockpitApi.CockpitActivity {
  // Find each activity type from the array
  const activeState = activityArray.find(item => item.activityType === 'ACTIVE') as CockpitActiveState | undefined;
  const hardcodedTenants = activityArray.find(item => item.activityType === 'HARDCODED_TENANT') as CockpitHardcodedTenant | undefined;
  const availableTenants = activityArray.find(item => item.activityType === 'AVAILABLE_TENANTS') as CockpitAvailableTenants | undefined;

  // Combine hardcoded and available tenants
  const allTenants = [
    //   ...(hardcodedTenants?.hardcodedTenants || []),
    ...(availableTenants?.availableTenants || [])
  ];

  // Find stencil and wrench tenants
  const stencilTenant = allTenants.filter(tenant => tenant.cockpitConfigTenantType === 'STENCIL');
  const wrenchTenant = allTenants.filter(tenant => tenant.cockpitConfigTenantType === 'WRENCH');

  return {
    availableTenants: {
      stencil: stencilTenant,
      wrench: wrenchTenant,
    },
    activeCockpitId: activeState?.activeId
  };
}



interface CockpitActiveState {
  activeId: string | undefined;
  activityType: 'ACTIVE';
}

interface CockpitHardcodedTenant {
  hardcodedTenants: CockpitApi.CockpitConfigTenant[];
  activityType: 'HARDCODED_TENANT';
}

interface CockpitAvailableTenants {
  availableTenants: CockpitApi.CockpitConfigTenant[];
  activityType: 'AVAILABLE_TENANTS';
}

// Merged union type
type CockpitActivityTypes =
  | CockpitActiveState
  | CockpitHardcodedTenant
  | CockpitAvailableTenants;