import React from 'react';
import { Typography, Box, Card, CardHeader, CardContent, CardActions, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import * as Burger from '@/burger'

import { FlowComposer } from '../flow';
import { DecisionComposer } from '../decision';
import { ServiceComposer } from '../service';

import ReleaseComposer from '../release';
import MigrationComposer from '../migration';

import composerVersion from '../version';
import { Composer } from '../context';
import { useWrenchNav } from '../nav';


interface ActivityData {
  type: "releases" | "decisions" | "flows" | "services" | "migration" | "templates" | "debug" | "compare";
  title: string;
  desc: string;
  buttonCreate: string;
  buttonViewAll?: string;
  buttonTertiary?: string;
  onView?: () => void;
  composer?: (handleClose: () => void) => React.ReactChild;
  onCreate?: () => void;
}

const createCards: (tabs: ReturnType<typeof useWrenchNav>) => ActivityData[] = (tabs) => ([
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
    onCreate: () => tabs.onNav({ type: 'DEBUG' }),
    onView: undefined,
    title: "activities.debug.title",
    desc: "activities.debug.desc",
    type: "debug",
    buttonCreate: "activities.debug.view",
    buttonViewAll: undefined,
  },
  {
    composer: (handleClose) => (<ReleaseComposer onClose={handleClose} />),
    onView: () => tabs.onNav({ type: 'RELEASES' }),
    title: "activities.releases.title",
    desc: "activities.releases.desc",
    type: "releases",
    buttonCreate: "buttons.create",
    buttonViewAll: "activities.releases.view",
  },
  {
    onCreate: () => tabs.onNav({ type: 'COMPARE' }),
    onView: undefined,
    title: "activities.compare.title",
    desc: "activities.compare.desc",
    type: "compare",
    buttonCreate: "activities.compare.view",
    buttonViewAll: undefined,
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
  const nav = useWrenchNav();

  const [open, setOpen] = React.useState<number>();
  const [coreVersion, setCoreVersion] = React.useState<{ version: string, built: string }>();
  const handleClose = () => setOpen(undefined);
  const { service } = Composer.useComposer();
  const cards = createCards(nav);

  let composer: undefined | React.ReactChild = undefined;
  let openComposer = open !== undefined ? cards[open].composer : undefined;
  if (openComposer) {
    composer = openComposer(handleClose);
  }

  React.useEffect(() => {
    service.version().then((version) => {
      setCoreVersion(version)
    });

  }, [service, setCoreVersion]);

  return (
    <>      
      {composer}
      <Burger.EveliActivities>
        {cards.map((card, index) => (
          <Card>
            <CardHeader title={<FormattedMessage id={card.title} />} />
            <CardContent><FormattedMessage id={card.desc} /></CardContent>
            <CardActions>
              {card.buttonViewAll && card.onView ? <Button variant='text' onClick={card.onView} children={<FormattedMessage id={card.buttonViewAll} />} /> : <Box />}
              <Button onClick={() => {
                if (card.composer) {
                  setOpen(index);
                } else if (card.onCreate) {
                  card.onCreate();
                }
              }} children={<FormattedMessage id={card.buttonCreate} />} />
            </CardActions>
          </Card>
        ))}
      </Burger.EveliActivities>
      <Typography variant="caption" sx={{ pt: 1 }} display={'flex'} flexDirection={'column'} alignItems={'center'}>
        <FormattedMessage id={"activities.version.composer"} values={{ version: composerVersion.tag, date: composerVersion.built }} />
        <Typography variant="caption" sx={{ pt: 1 }} >
          <FormattedMessage id={"activities.version.core"} values={{ version: coreVersion?.version, date: coreVersion?.built }} />
        </Typography>
      </Typography>
    </>
  );
}

export { Activities };
