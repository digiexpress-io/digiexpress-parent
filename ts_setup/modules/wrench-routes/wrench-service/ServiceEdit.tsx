import React from 'react';
import { Box } from '@mui/material';

import MonacoReact from '@monaco-editor/react';
import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';


const ServiceEdit: React.FC<{service: HdesApi.Entity<HdesApi.AstService>}> = ({service}) => {
  const { actions, session } = Composer.useComposer();

  const handleChange = (value: string | undefined) => {
    actions.handlePageUpdate(service.id, value ?? '')
  }
  
  const update = session.pages[service.id];
  const src: string = (update && update.value ? update.value : service.ast?.value) as string;

  return (<Box height="calc(100vh - 64px)">
    <MonacoReact 
      onChange={handleChange}
      value={src ? src : "#--failed-to-parse"}
      defaultLanguage='java'/>
  </Box>);
}

export { ServiceEdit };
