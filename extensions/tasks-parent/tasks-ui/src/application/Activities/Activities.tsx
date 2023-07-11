import React from 'react';
import { Typography, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';
import Styles from '@styles';


type ActivityId = "tasks";


const createActivities: (props: {
  actions: Burger.TabsActions,
  setOpen: (index: ActivityId) => void
}) => Styles.StyledCardItemProps[] = ({ actions, setOpen }) => ([

  { id: "tasks", 
    title: "activities.tasks.title",
    content: {
      label: "activities.tasks.desc"
    },
    primary: { 
      label: "buttons.view", 
      onClick: () => actions.handleTabAdd({id: 'tasks', label: <FormattedMessage id="activities.tasks.title"/>}) 
    }
  },

  { id: "group", 
    title: "activities.group.title",
    content: {
      label: "activities.group.desc"
    },
    primary: { 
      label: "buttons.view", 
      onClick: () => actions.handleTabAdd({id: 'group', label: <FormattedMessage id="activities.group.title"/>}) 
    }
  },

  { id: "mytasks", 
    title: "activities.mytasks.title",
    content: {
      label: "activities.mytasks.desc"
    },
    primary: { 
      label: "buttons.view", 
      onClick: () => actions.handleTabAdd({id: 'mytasks', label: <FormattedMessage id="activities.mytasks.title"/>}) 
    }
  },

  { id: "myhistory", 
    title: "activities.myhistory.title",
    content: {
      label: "activities.myhistory.desc"
    },
    primary: { 
      label: "buttons.view", 
      onClick: () => actions.handleTabAdd({id: 'myhistory', label: <FormattedMessage id="activities.myhistory.title"/>}) 
    }
  },

  { id: "search", 
    title: "activities.search.title",
    content: {
      label: "activities.search.desc"
    },
    primary: { 
      label: "buttons.view", 
      onClick: () => actions.handleTabAdd({id: 'search', label: <FormattedMessage id="activities.search.title"/>}) 
    }
  },

  { id: "reporting", 
    title: "activities.reporting.title",
    content: {
      label: "activities.reporting.desc"
    },
    primary: { 
      label: "buttons.view", 
      onClick: () => actions.handleTabAdd({id: 'reporting', label: <FormattedMessage id="activities.reporting.title"/>}) 
    }
  },

  
  
]);

//card view for all CREATE views
const Activities: React.FC<{}> = () => {
  const { actions } = Burger.useTabs();
  const [open, setOpen] = React.useState<ActivityId>();
  const cards = React.useMemo(() => createActivities({ actions, setOpen }), [actions, setOpen]);
  const handleClose = () => setOpen(undefined)


  return (<Styles.Cards title="activities.title" desc="activities.desc" items={cards} />);
}

export { Activities };
