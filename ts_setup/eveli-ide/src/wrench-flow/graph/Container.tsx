import React from 'react';
import { Box } from '@mui/material';

import { Vis } from '../../wrench-vis';
import GraphAPI from './GraphAPI';
import { HdesApi } from '@/burger';



interface ContainerProps {
  flow:HdesApi.AstFlow;
  site:HdesApi.Site;
  onClick: (id: string) => void;
  onDoubleClick: (id: string) => void;
};

const Container: React.FC<ContainerProps> = ({ site, flow, onClick, onDoubleClick }) => {
  
  const events = { onClick, onDoubleClick };
  const model = React.useMemo(() => GraphAPI.create({ fl: flow, models: site }), [flow, site]);

  return (<Box sx={{
    height: "calc(100vh - 64px)",
    width: '70vh',
    backgroundColor: 'transparent'
  }}>{model ? <Vis id={flow.name} events={events} model={model} /> : null}
  </Box>);
}

export type { ContainerProps };
export { Container };
