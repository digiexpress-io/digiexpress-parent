import React from 'react';

import { LocaleProvider, ConfigProvider } from '@dxs-ts/gamut';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query'

import { DemoTheme } from './theme';



export const DemoApp: React.FC<{ children: any }> = ({ children }) => {
  const queryClient = new QueryClient()

  const iamLiveness = 600000000;
  const staleTime = 600000000;
  function handleExpire() {
    alert("SESSION EXPIRED OOPS");
  }

  return (
    <QueryClientProvider client={queryClient}>
      <LocaleProvider disableErrors>
        <DemoTheme>
          <ConfigProvider options={{ handleExpire, iamLiveness, staleTime }}>
            {children}
          </ConfigProvider>
        </DemoTheme>
      </LocaleProvider>
    </QueryClientProvider>);
}



