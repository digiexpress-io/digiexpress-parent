

import React from "react";
import { useQuery } from "@tanstack/react-query";

import { UserProfileApi } from '@dxs-ts/user-profile'
import { TableState, TableStateInitWith } from "@dxs-ts/xui-table";

const default_profile = 'default';

export interface SavedFilter {
  id: string;
  name: string;
  filter: TableStateInitWith;
}

export function useSavedTableFilters(tableId: string) {
  const { userId, backend } = UserProfileApi.useUserProfile();
  const settingId = `saved-table-filters-${tableId}`;

  const { data, error, refetch, isPending } = useQuery({
    queryKey: [settingId],
    queryFn: async (): Promise<SavedFilter[]> => {
      const data = await backend.findUiSettings(settingId);
      if(!data?.config) {
        return [];
      }
      try {
        return data.config.map(e => ({
          id: e.dataId,
          name: e.label ?? '-',
          filter: JSON.parse(e.value)
        }));
      } catch(e) {
        console.error('Failed to parse table settings', e)
      }
      return [];
    }
  });

  async function onSave(
    next: TableState, 
    operation?: (
      { dataId: string, label: string, type: 'UPDATE' } |
      { label: string, type: 'CREATE' } |
      { dataId: string, type: 'DELETE' }
    )
): Promise<void> {

    let toBeSaved: UserProfileApi.UiSettingsForConfig | undefined;
    let dataId: string;
    if(operation?.type === 'CREATE') {
      dataId = `${data?.length ?? 1}`;
      toBeSaved = {
        dataId: dataId,
        label: operation?.label ?? default_profile,
        value: JSON.stringify(next.copy())
      };
    } else if (operation?.type === 'UPDATE') {
      dataId = operation.dataId;
      toBeSaved = {
        dataId,
        label: operation.label,
        value: JSON.stringify(next.copy())
      };
    } else if (operation?.type === 'DELETE') {
      dataId = operation.dataId;
    } else {
      throw new Error("not implemented")
    }

    const configs = (data ?? []).reduce<Record<string, UserProfileApi.UiSettingsForConfig>>((collector, next) => {
      if (next.id === dataId && !toBeSaved) {
        return collector;
      } else if (next.id === dataId) {
        collector[dataId] = toBeSaved!;
      } else {
        collector[next.id] = { dataId: next.id, label: next.name, value: JSON.stringify(next.filter) };
      }
      return collector;
    }, {});

    if (!configs[dataId] && toBeSaved) {
      configs[dataId] = toBeSaved;
    }


    const _updated = await backend.updateUiSettings({
      commandType: 'UpsertUiSettings',
      settingsId: settingId,
      userId: userId,
      visibility: [],
      config: Object.values(configs)
    });

    const _refreshed = await refetch();
  };  

  const onRestore: () => Promise<TableStateInitWith | undefined> = React.useCallback(async () => {
    return undefined;
  }, [tableId]);

  return { onSave, onRestore, filters: data };
}
