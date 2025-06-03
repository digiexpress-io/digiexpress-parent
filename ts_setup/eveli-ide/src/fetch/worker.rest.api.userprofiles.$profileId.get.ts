import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { PrefsApi } from '@/api-prefs';


export const Hook = createFileFetch('worker/rest/api/userprofiles/$profileId.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;
  
  return {
    restApi: (): PrefsApi.PrefsRestApi => ({
      async currentUserProfile(): Promise<PrefsApi.UserProfile> {
        const baseUrl = url({ profileId: 'current' });
        return params.fetch(`baseUrl`).then(response => response.json());
      },
      async getUserProfileById(profileId: string): Promise<PrefsApi.UserProfile> {
        const baseUrl = url({ profileId });
        return params.fetch(baseUrl).then(response => response.json());
      },
      async findAllUserProfiles(): Promise<PrefsApi.UserProfile[]> {
        const baseUrl = url({ profileId: '' });
        return params.fetch(baseUrl).then(response => response.json());
      },
      async updateUserProfile(profileId: string, commands: PrefsApi.UserProfileUpdateCommand<any>[]): Promise<PrefsApi.UserProfile> {
        const baseUrl = url({ profileId: 'current' });
        return params.fetch(baseUrl, { method: 'PUT', body: JSON.stringify(commands)}).then(response => response.json());
      },
      async updateUiSettings(commands: PrefsApi.UpsertUiSettings): Promise<PrefsApi.UserProfile> {
        const baseUrl = url({ profileId: 'current/ui-settings' });
        return params.fetch(baseUrl, { method: 'PUT', body: JSON.stringify(commands)}).then(response => response.json());
      },
      findUiSettings(settingsId: string): Promise<PrefsApi.UiSettings | undefined> {
        const baseUrl = url({ profileId: 'current/ui-settings/' + settingsId});
        return params.fetch(baseUrl).then(response => response.json());
      }
    })
  }
}