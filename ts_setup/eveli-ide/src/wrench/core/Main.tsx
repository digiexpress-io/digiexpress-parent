import React from 'react';
import { Box } from '@mui/material';
import { SxProps } from '@mui/system';

import Activities from './activities';

import { Composer } from './context';
import { FlowEdit, FlowsView } from './flow';
import { DecisionEdit } from './decision';
import { ServiceEdit } from './service';
import { DebugView } from './debug';
import { ServicesView } from './service/ServicesView';
import { ReleasesView } from './release';

import { CompareView } from './compare';
import { DecisionsView } from './decision/DecisionsView';
import { useWrenchNav } from './nav';


const root: SxProps = { height: `100%`,  padding: 1, backgroundColor: "primary.contrastText" };


const Main: React.FC<{}> = () => {
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
      case 'RELEASES': return (<Box sx={root}><ReleasesView /></Box>);
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
        if (entity.source.bodyType === 'DT') {
          return (<Box sx={root}><DecisionEdit decision={entity} /></Box>);
        } else if (entity.source.bodyType === 'FLOW') {
          return (<Box sx={root}><FlowEdit flow={entity} /></Box>);
        } else if (entity.source.bodyType === 'FLOW_TASK') {
          return (<Box sx={root}><ServiceEdit service={entity} /></Box>);
        }
        return (<></>)
      }
      default: return (<></>)
    }

  }, [activeItem, site, session]);
}
export { Main }


