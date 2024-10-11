import React from 'react';
import { Box } from '@mui/material';

import CodeEditor from '../../code-editor';
import { Composer } from '../context';
import { HdesApi } from '../client';

const ServiceEdit: React.FC<{service: HdesApi.Entity<HdesApi.AstService>}> = ({service}) => {
  const { actions, session } = Composer.useComposer();

  const handleChange = (value: string | undefined) => {
    actions.handlePageUpdate(service.id, [{ type: "SET_BODY", value: value }])
  }
  
  const update = session.pages[service.id];
  const src = update && update.value.length > 0 ? update.value[0].value : service.ast?.value;

  return (<Box height="calc(100vh - 64px)">
    <CodeEditor id={service.id} mode="groovy" src={src ? src : "#--failed-to-parse"} onChange={handleChange} />
  </Box>);
}

export { ServiceEdit };
