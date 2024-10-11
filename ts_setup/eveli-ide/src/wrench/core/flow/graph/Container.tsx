import React from 'react';
import { Box } from '@mui/material';

import Vis from '../../../vis';
import GraphAPI from './GraphAPI';
import { HdesApi as Client } from '../../client';


const options = {
  layout: {
    //      hierarchical: {
    //        enabled: true,
    //        levelSeparation: 100,
    //        parentCentralization: true,
    //        direction: 'UD',
    //        sortMethod: 'directed',
    //        nodeSpacing: 250,
    //        treeSpacing: 200,
    //      }
  },
  physics: {
    enabled: false
  },
  nodes: {
    shape: 'box',
    margin: 10,
    widthConstraint: {
      maximum: 200
    },
    shadow: {
      enabled: true, size: 10, x: 5, y: 5,
      color: 'rgba(0,0,0,0.5)',
    }
  },
  edges: {
    //color: '#fff',
    font: {
      size: 12
    },
    widthConstraint: {
      maximum: 90
    }
  }
}

interface ContainerProps {
  flow: Client.AstFlow;
  site: Client.Site;
  onClick: (id: string) => void;
  onDoubleClick: (id: string) => void;
};

const Container: React.FC<ContainerProps> = ({ site, flow, onClick, onDoubleClick }) => {
  
  const events = { onClick, onDoubleClick };
  const model = React.useMemo(() => GraphAPI.create({ fl: flow, models: site }), [flow, site]);

  return (<Box sx={{
    height: "calc(100vh - 64px)",
    width: '50vh',
    backgroundColor: 'transparent'
  }}>{model ? Vis.create({ events, options, model }) : null}
  </Box>);
}

export type { ContainerProps };
export { Container };
