import React from 'react';
import { Box } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';

import { Vis } from '../vis';
import GraphAPI from './GraphAPI';


interface ContainerProps {
  flow: any;
  site: Fs.WrenchBody;
  onClick: (id: string) => void;
  onDoubleClick: (id: string) => void;
};

const Container: React.FC<ContainerProps> = ({ site, flow, onClick, onDoubleClick }) => {

  const events = { onClick, onDoubleClick };
  const model = React.useMemo(() => GraphAPI.create({ fl: flow, models: site }), [flow, site]);

  return (<Box sx={{
    height: "100%",
    width: '100%',
    backgroundColor: 'transparent'
  }}>{model ? <Vis id={flow.name} events={events} model={model} /> : null}
  </Box>);
}

export type { ContainerProps };
export { Container };
