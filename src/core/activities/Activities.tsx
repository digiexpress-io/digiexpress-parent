import React from 'react';
import { Typography, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';

import {FlowComposer} from '../flow';
import {DecisionComposer} from '../decision';
import {ServiceComposer} from '../service';

import ReleaseComposer from '../release';
import MigrationComposer from '../migration';
import TemplateComposer from '../template';

import { ActivityItem, ActivityData } from './ActivityItem';

import composerVersion from '../version';
import { Composer } from '../context';

interface ActivityType {
  type: "releases" | "decisions" | "flows" | "services" | "migration" | "templates" | "debug";
  composer?: (handleClose: () => void) => React.ReactChild;
  onCreate?: () => void;
}

const createCards: (tabs: Burger.TabsActions) => (ActivityData & ActivityType)[] = (tabs) => ([
  {
    composer: (handleClose) => (<FlowComposer onClose={handleClose} />),
    onView: undefined,
    title: "activities.flows.title",
    desc: "activities.flows.desc",
    type: "flows",
    buttonCreate: "buttons.create",
  },
  {
    composer: (handleClose) => (<DecisionComposer onClose={handleClose} />),
    onView: undefined,
    title: "activities.decisions.title",
    desc: "activities.decisions.desc",
    type: "decisions",
    buttonCreate: "buttons.create",
    buttonViewAll: undefined
  },
  {
    composer: (handleClose) => (<ServiceComposer onClose={handleClose} />),
    onView: undefined,
    title: "activities.services.title",
    desc: "activities.services.desc",
    type: "services",
    buttonCreate: "buttons.create",
    buttonViewAll: undefined
  },
  {
    onCreate: () => tabs.handleTabAdd({ id: 'debug', label: "Debug" }),
    onView: undefined,
    title: "activities.debug.title",
    desc: "activities.debug.desc",
    type: "debug",
    buttonCreate: "activities.debug.view",
    buttonViewAll: undefined,
  },
  {
    composer: (handleClose) => (<ReleaseComposer onClose={handleClose} />),
    onView: () => tabs.handleTabAdd({ id: 'releases', label: "Releases" }),
    onTertiary: () => tabs.handleTabAdd({ id: 'graph', label: "Release Graph" }),
    title: "activities.releases.title",
    desc: "activities.releases.desc",
    type: "releases",
    buttonCreate: "buttons.create",
    buttonViewAll: "activities.releases.view",
    buttonTertiary: "activities.releases.graph"
  },
  {
    composer: (handleClose) => <TemplateComposer onClose={handleClose} />,
    onView: () => tabs.handleTabAdd({ id: 'templates', label: "Templates" }),
    title: "activities.templates.title",
    desc: "activities.templates.desc",
    type: "templates",
    buttonCreate: "buttons.create",
    buttonViewAll: "activities.templates.view"
  },
  {
    composer: (handleClose) => <MigrationComposer onClose={handleClose} />,
    onView: undefined,
    title: "activities.migration.title",
    desc: "activities.migration.desc",
    type: "migration",
    buttonCreate: "buttons.create",
    buttonViewAll: undefined
  },
]);

//card view for all CREATE views
const Activities: React.FC<{}> = () => {
  const { actions } = Burger.useTabs();
  const [open, setOpen] = React.useState<number>();
  const [coreVersion, setCoreVersion] = React.useState<{version: string, built: string}>();
  const handleClose = () => setOpen(undefined);
  const cards = React.useMemo(() => createCards(actions), [actions]);
  const { service } = Composer.useComposer();

  let composer: undefined | React.ReactChild = undefined;
  let openComposer = open !== undefined ? cards[open].composer : undefined;
  if(openComposer) {
    composer = openComposer(handleClose);
  }

  React.useEffect(() => {
    service.version().then((version) => {
      console.log("hdes core version", version, "hdes composer version", composerVersion);
      setCoreVersion(version)
    }); 

  }, [service, setCoreVersion]);
  
  return (
    <>
      <Typography variant="h3" fontWeight="bold" sx={{ p: 1, m: 1 }}>
        <FormattedMessage id={"activities.title"} />
        <Typography variant="body2" sx={{ pt: 1 }}>
          <FormattedMessage id={"activities.desc"} />
        </Typography>
      </Typography>
      <Box sx={{ margin: 1, display: 'flex', flexWrap: 'wrap', justifyContent: 'center' }}>
        {composer}
        {cards.map((card, index) => (<ActivityItem key={index} data={card} onCreate={() => {
          if(card.composer) {
             setOpen(index);
          } else if(card.onCreate) {
            card.onCreate();
          }
        }} />))}
      </Box>
      <Typography variant="caption" sx={{ pt: 1 }} display={'flex'} flexDirection={'column'} alignItems={'center'}>
          <FormattedMessage id={"activities.version.composer"} values={{ version: composerVersion.tag, date: composerVersion.built}}/>
          <Typography variant="caption" sx={{ pt: 1 }} >
            <FormattedMessage id={"activities.version.core"} values={{ version: coreVersion?.version, date: coreVersion?.built}}/>
          </Typography>
      </Typography>
    </>
  );
}

export { Activities };
