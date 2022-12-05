import React from 'react';
import { Typography, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';

import { ActivityItem, ActivityData } from './ActivityItem';
import MigrationComposer from '../migration';

interface ActivityType {
  type: "migration";
  composer?: (handleClose: () => void) => React.ReactChild;
  onCreate?: () => void;
}

const createCards: (tabs: Burger.TabsActions) => (ActivityData & ActivityType)[] = (tabs) => ([
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
  if (openComposer) {
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
          if (card.composer) {
            setOpen(index);
          } else if (card.onCreate) {
            card.onCreate();
          }
        }} />))}
      </Box>
    </>
  );
}

export { Activities };
