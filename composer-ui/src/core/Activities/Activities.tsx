import React from 'react';
import { Typography, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';
import Styles from '@styles';

import MigrationComposer from '../Migration';

type ActivityId = "migration" | "project";


const createActivities: (props: {
  actions: Burger.TabsActions,
  setOpen: (index: ActivityId) => void
}) => Styles.StyledCardItemProps[] = ({ actions, setOpen }) => ([

  { id: "project", title: "activities.project.title",
    content: {
      label: "activities.project.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({id: 'project', label: "project"})
    }
  },

  { id: "migration", title: "activities.migration.title",
    content: {
      label: "activities.migration.desc"
    },
    primary: {
      label: "buttons.create",
      onClick: () => setOpen('migration')
    }
  },
]);

//card view for all CREATE views
const Activities: React.FC<{}> = () => {
  const { actions } = Burger.useTabs();
  const [open, setOpen] = React.useState<ActivityId>();
  const cards = React.useMemo(() => createActivities({ actions, setOpen }), [actions, setOpen]);
  const handleClose = () => setOpen(undefined)


  return (<>
    {open === 'migration' ? <MigrationComposer onClose={handleClose} /> : undefined}
    {open === 'project' ? <MigrationComposer onClose={handleClose} /> : undefined}
    <Styles.Cards title="activities.title" desc="activities.desc" items={cards} />
  </>);
}

export { Activities };
