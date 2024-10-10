import React from 'react';
import { Box } from '@mui/material';
import { SxProps } from '@mui/system';

import Burger from '@/burger';

import Activities from './activities';
import { Composer } from './context';
import { FlowEdit } from './flow';
import { DecisionEdit } from './decision';
import { ServiceEdit } from './service';
import { DebugView } from './debug';
import { ReleasesView } from './release';

import { Client } from './context';
import { CompareView } from './compare';



const root: SxProps = { height: `100%`, backgroundColor: "mainContent.main" };

const EntityEditor: React.FC<{ entity: Client.Entity<any> }> = ({ entity }) => {
  if (entity.source.bodyType === 'DT') {
    return (<DecisionEdit decision={entity} />);
  } else if (entity.source.bodyType === 'FLOW') {
    return (<FlowEdit flow={entity} />);
  } else if (entity.source.bodyType === 'FLOW_TASK') {
    return (<ServiceEdit service={entity} />);
  }
  return null;
}

const Main: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const { site, session } = Composer.useComposer();
  const tabs = layout.session.tabs;
  const active = tabs.length ? tabs[layout.session.history.open] : undefined;
  const entity = active ? session.getEntity(active.id) : undefined;

  //composers which are NOT linked directly with an article

  return React.useMemo(() => {
    if (site.contentType === "NO_CONNECTION") {
      return (<Box>{site.contentType}</Box>);
    }
    if (!active) {
      return null;
    }
    if (active.id === 'activities') {
      return (<Box sx={root}><Activities /></Box>);
    } else if (active.id === 'releases') {
      return (<Box sx={root}><ReleasesView /></Box>);
    } else if (active.id === 'graph') {
      return (<Box sx={root}>graph</Box>);
    } else if (active.id === 'templates') {
      return (<Box sx={root}>templates</Box>);
    } else if (active.id === 'debug') {
      return (<Box sx={root}><DebugView /></Box>);
    } else if (active.id === 'compare') {
      return (<Box sx={root}><CompareView /></Box>);
    }
    if (entity) {
      console.log(entity);
      return <Box sx={root}><EntityEditor entity={entity} /></Box>
    }
    throw new Error("unknown view: " + JSON.stringify(active, null, 2));

  }, [active, site, entity]);
}
export { Main }


