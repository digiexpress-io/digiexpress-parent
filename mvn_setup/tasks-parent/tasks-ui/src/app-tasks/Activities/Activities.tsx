import React from 'react';
import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import { StyledCardItemProps, StyledCards } from './StyledCards';


type ActivityId = "tasks";


const createActivities: (props: {
  actions: Burger.TabsActions,
  setOpen: (index: ActivityId) => void
}) => StyledCardItemProps[] = ({ actions, setOpen }) => ([

  {
    id: "teamSpace",
    title: "activities.teamSpace.title",
    content: {
      label: "activities.teamSpace.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'teamSpace', label: <FormattedMessage id="activities.teamSpace.title" /> })
    }
  },

  {
    id: "mytasks",
    title: "activities.mytasks.title",
    content: {
      label: "activities.mytasks.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'mytasks', label: <FormattedMessage id="activities.mytasks.title" /> })
    }
  },

  {
    id: "myhistory",
    title: "activities.myhistory.title",
    content: {
      label: "activities.myhistory.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'myhistory', label: <FormattedMessage id="activities.myhistory.title" /> })
    }
  },

  {
    id: "search",
    title: "activities.search.title",
    content: {
      label: "activities.search.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'search', label: <FormattedMessage id="activities.search.title" /> })
    }
  },

  {
    id: "reporting",
    title: "activities.reporting.title",
    content: {
      label: "activities.reporting.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'reporting', label: <FormattedMessage id="activities.reporting.title" /> })
    }
  },


  {
    id: "migration",
    title: "activities.migration.title",
    content: {
      label: "activities.migration.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'migration', label: <FormattedMessage id="activities.migration.title" /> })
    }
  },

  {
    id: "archive",
    title: "activities.archive.title",
    content: {
      label: "activities.archive.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'archive', label: <FormattedMessage id="activities.archive.title" /> })
    }
  },

  {
    id: "inbox",
    title: "activities.inbox.title",
    content: {
      label: "activities.inbox.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'inbox', label: <FormattedMessage id="activities.inbox.title" /> })
    }
  },

]);

//card view for all CREATE views
const Activities: React.FC<{}> = () => {
  const { actions } = Burger.useTabs();
  const [open, setOpen] = React.useState<ActivityId>();
  const cards = React.useMemo(() => createActivities({ actions, setOpen }), [actions, setOpen]);
  const handleClose = () => setOpen(undefined)


  return (<StyledCards title="activities.title" desc="activities.desc" items={cards} />);
}

export { Activities };
