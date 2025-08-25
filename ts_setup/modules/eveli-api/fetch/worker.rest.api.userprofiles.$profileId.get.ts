import React from 'react';
import { createFileFetch } from '@dxs-ts/envir-fetch';
import { UserProfileApi } from '@dxs-ts/user-profile';



export const Hook = createFileFetch('worker/rest/api/userprofiles/$profileId.GET')({
  hook
})


function hook(props: {}) {
  const params = Hook.useParams();
  const restApi: UserProfileApi.PrefsRestApi = React.useMemo(() => ({
    async currentUserProfile(createIfNotDefined?: boolean): Promise<UserProfileApi.UserProfile> {
      const { url } = params;
      const baseUrl = url({ profileId: 'current' + (createIfNotDefined ? '?create=true' : '') });
      return params.fetch(`${baseUrl}`)
        .then(async (response) => {
          const length = response.headers.get('Content-Length');
          if (length === '0') {
            return undefined;
          }
          return response.json();
        });
    },
    async getUserProfileById(profileId: string): Promise<UserProfileApi.UserProfile> {
      const { url } = params;
      const baseUrl = url({ profileId });
      return params.fetch(baseUrl)
        .then(async (response) => {
          const length = response.headers.get('Content-Length');
          if (length === '0') {
            return undefined;
          }
          return response.json();
        });
    },
    async findAllUserProfiles(): Promise<UserProfileApi.UserProfile[]> {
      const { url } = params;
      const baseUrl = url({ profileId: '' });
      return params.fetch(baseUrl).then(response => response.json());
    },
    async updateUserProfile(commands: UserProfileApi.UserProfileUpdateCommand<any>[]): Promise<UserProfileApi.UserProfile> {
      const { url } = params;
      const baseUrl = url({ profileId: 'current' });
      return params.fetch(baseUrl, { method: 'PUT', body: JSON.stringify(commands) }).then(response => response.json());
    },
    async updateUiSettings(commands: UserProfileApi.UpsertUiSettings): Promise<UserProfileApi.UserProfile> {
      const { url } = params;
      const baseUrl = url({ profileId: 'current/ui-settings' });
      return params.fetch(baseUrl, { method: 'PUT', body: JSON.stringify(commands) }).then(response => response.json());
    },
    async findUiSettings(settingsId: string): Promise<UserProfileApi.UiSettings | undefined> {
      const { url } = params;
      const baseUrl = url({ profileId: 'current/ui-settings/' + settingsId });
      return params.fetch(baseUrl)
        .then(async (response) => {
          const length = response.headers.get('Content-Length');
          if (length === '0') {
            return undefined;
          }
          return response.json();
        });
    }
  }), [])

  return { restApi }
}