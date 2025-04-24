import React from 'react';


import { EveliShellMiniBarRoot, EveliShellMiniBarClassName } from '@/eveli-shell/useUtilityClasses';
import { LocaleFilter } from '@/stencil-explorer';

export const Toolbar: React.FC<{}> = () => {


  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName} sx={{
      display:'flex', flexDirection: 'column', justifyContent: 'end', flexGrow: 1
    }}>
      <LocaleFilter />
    </EveliShellMiniBarRoot>
  );
}