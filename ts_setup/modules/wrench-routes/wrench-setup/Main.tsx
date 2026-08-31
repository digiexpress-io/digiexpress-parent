import React from 'react';
import { Box } from '@mui/material';
import { SxProps } from '@mui/system';

import { Activities } from './Activities';
import { WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';

import { FlowEdit, FlowsView } from '../wrench-flow';
import { DecisionEdit, DecisionsView } from '../wrench-decision';
import { ServiceEdit } from '../wrench-service';
import { DebugView } from '../wrench-debug';
import { ServicesView } from '../wrench-service/ServicesView';
import { CompareView } from '../wrench-compare';
import { useWrenchNav } from '../wrench-nav';


const root: SxProps = { height: `100%`,  padding: 1, backgroundColor: "primary.contrastText" };


export const Main: React.FC<{}> = () => {
  const { site, session } = Composer.useComposer();
  const { activeItem } = useWrenchNav();

  return React.useMemo(() => {
    
    if (site.contentType === "NO_CONNECTION") {
      return (<Box>{site.contentType}</Box>);
    }


    if (!activeItem) {
      return (<Box sx={root}></Box>)
    }
    switch (activeItem.type) {
      case 'ACTIVITIES': return (<Box sx={root}><Activities /></Box>);
      case 'DEBUG': return (<Box sx={root}><DebugView /></Box>);
      case 'SERVICES': return (<Box sx={root}><ServicesView /></Box>);
      case 'COMPARE': return (<Box sx={root}><CompareView /></Box>);
      case 'FLOWS': return (<Box sx={root}><FlowsView /></Box>);
      case 'DECISIONS': return (<Box sx={root}><DecisionsView /></Box>);
      case 'ENTITY_EDITOR': {
        const entity = session.getEntity(activeItem.id);
        if (!entity) {
          return (<>not found</>)
        }
        try {
          if (entity.ast.bodyType === 'DECISION_TABLE') {
            return (<Box sx={root}><DecisionEdit decision={entity} /></Box>);
          } else if (entity.ast.bodyType === 'FLOW') {
            return (<Box sx={root}><FlowEdit flow={entity} /></Box>);
          } else if (entity.ast.bodyType === 'FLOW_TASK') {
            return (<Box sx={root}><ServiceEdit service={entity} /></Box>);
          }
        } catch(e) {
          console.error(e);
          return (<>{JSON.stringify(entity)}</>)
        }
        return (<></>)
      }
      default: return (<></>)
    }

  }, [activeItem, site, session]);
}