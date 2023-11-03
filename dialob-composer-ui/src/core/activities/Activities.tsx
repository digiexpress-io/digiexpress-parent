import React from 'react';
import { Typography, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';

import {FormComposer} from '../form-composer';

import ReleaseComposer from '../release';
import MigrationComposer from '../migration';
import TemplateComposer from '../template';

import { ActivityItem, ActivityData } from './ActivityItem';

interface ActivityType {
  type: "releases" | "revs" | "forms" | "migration" | "templates" | "debug" | "locales" | "globallists" | "datatypes";
  composer?: (handleClose: () => void) => React.ReactChild;
  onCreate?: () => void;
}

const createCards: (tabs: Burger.TabsActions) => (ActivityData & ActivityType)[] = (tabs) => ([
  {
    composer: (handleClose) => (<FormComposer onClose={handleClose} />),
    onView: undefined,
    title: "activities.datatypes.title",
    desc: "activities.datatypes.desc",
    type: "datatypes",
    buttonCreate: "buttons.create",
  },
  {
    composer: (handleClose) => (<FormComposer onClose={handleClose} />),
    onView: undefined,
    title: "activities.globallists.title",
    desc: "activities.globallists.desc",
    type: "globallists",
    buttonCreate: "buttons.create",
  },
  {
    composer: (handleClose) => (<FormComposer onClose={handleClose} />),
    onView: undefined,
    title: "activities.locales.title",
    desc: "activities.locales.desc",
    type: "locales",
    buttonCreate: "buttons.create",
  },
  {
    composer: (handleClose) => (<FormComposer onClose={handleClose} />),
    onView: undefined,
    title: "activities.debug.title",
    desc: "activities.debug.desc",
    type: "debug",
    buttonCreate: "buttons.create",
  },
  {
    composer: (handleClose) => (<FormComposer onClose={handleClose} />),
    onView: undefined,
    title: "activities.revs.title",
    desc: "activities.revs.desc",
    type: "revs",
    buttonCreate: "buttons.create",
  },
  {
    composer: (handleClose) => (<FormComposer onClose={handleClose} />),
    onView: undefined,
    title: "activities.forms.title",
    desc: "activities.forms.desc",
    type: "forms",
    buttonCreate: "buttons.create",
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
  const handleClose = () => setOpen(undefined);
  const cards = React.useMemo(() => createCards(actions), [actions]);

  let composer: undefined | React.ReactChild = undefined;
  let openComposer = open !== undefined ? cards[open].composer : undefined;
  if(openComposer) {
    composer = openComposer(handleClose);
  }

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
    </>
  );
}

export { Activities };
