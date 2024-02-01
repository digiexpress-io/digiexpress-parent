import React from 'react';

import { usePreference, PreferenceProvider, PreferenceInit } from 'descriptor-prefs';
import { CustomerDescriptor  } from 'descriptor-customer';


export type ColumnName = keyof CustomerDescriptor;

export const ColumnNameOptions: ColumnName[] = [
  'displayName',
  'customerType',
  'created',
  'lastLogin',
  'tasks'
];


function initPrefs(): PreferenceInit {
  return {
    id: 'customer-search-page',
    fields: ColumnNameOptions,
    sorting: {
      dataId: 'lastLogin',
      direction: 'desc'
    }
  }
}
export const useCustomerPrefs = usePreference;


export const CustomerPrefsProvider: React.FC<{children: React.ReactNode}> = ({children}) => {
  return (<PreferenceProvider init={initPrefs()}><>{children}</></PreferenceProvider>);
}