import React from 'react'
import { TableConfig, TableProvider, TableSettings } from '@dxs-ts/xui-table'
import { UserProfileApi } from '@dxs-ts/user-profile'
import { useTenantConfigFeatures } from '../api-tenant-config'




export const SmartTableIntegration: React.FC<{ children: React.ReactNode }> = (props) => {

  const { isEnabled } = useTenantConfigFeatures()
  const isSmartTables = isEnabled('SMART_TABLES');

  if(isSmartTables) {
    return (<SmartTableIntegrationEnabled>{ props.children }</SmartTableIntegrationEnabled>);
  } else {
    return (<TableProvider>{props.children}</TableProvider>);
  } 
}

const SmartTableIntegrationEnabled: React.FC<{ children: React.ReactNode }> = (props) => {

  const { backend, userId } = UserProfileApi.useUserProfile();
  const persistent =  React.useMemo(() => {
    return {
      find: async (settingsId: string): Promise<TableSettings | undefined> => backend.findUiSettings(settingsId),
      save: async (settingsId: string, config: TableConfig[]): Promise<any>  => {
        return backend.updateUiSettings({
          commandType: 'UpsertUiSettings',
          settingsId,
          userId,
          visibility: [],
          config
        })
      }
    }


  }, [userId, backend])

  return (<TableProvider persistent={persistent}>{props.children}</TableProvider>);
}