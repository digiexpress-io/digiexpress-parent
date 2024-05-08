import React from 'react';

import { PreferenceInit } from 'descriptor-prefs';
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


export const CustomerPrefsProvider: React.FC<{children: React.ReactNode}> = ({children}) => {
  return (<><>{children}</></>);
}