import React from 'react';
import { EveliShellMiniBarRoot, EveliShellMiniBarClassName } from '@dxs-ts/eveli-primitives';


export const Toolbar: React.FC<{}> = () => {
  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName} sx={{
      display:'flex', 
      flexDirection: 'column', 
      justifyContent: 'end', 
      flexGrow: 1
    }}>
    </EveliShellMiniBarRoot>
  );
}