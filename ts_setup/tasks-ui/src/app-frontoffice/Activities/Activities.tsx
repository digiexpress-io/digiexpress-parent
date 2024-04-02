import React from 'react';
import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import { StyledCardItemProps, StyledCards } from './StyledCards';


const createActivities: (props: {
  actions: Burger.TabsActions,
}) => StyledCardItemProps[] = ({ actions }) => ([
  {
    id: "deployments",
    title: "activities.frontoffice.deployments.title",
    content: {
      label: "activities.frontoffice.deployments.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'deployments', label: <FormattedMessage id="activities.frontoffice.deployments.title" /> })
    }
  },
  {
    id: "crm",
    title: "activities.frontoffice.crm.title",
    content: {
      label: "activities.frontoffice.crm.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'crm', label: <FormattedMessage id="activities.frontoffice.crm.title" /> })
    }
  },
  {
    id: "tasks",
    title: "activities.frontoffice.tasks.title",
    content: {
      label: "activities.frontoffice.tasks.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'tasks', label: <FormattedMessage id="activities.frontoffice.tasks.title" /> })
    }
  },
  {
    id: "stencil",
    title: "activities.frontoffice.stencil.title",
    content: {
      label: "activities.frontoffice.stencil.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'stencil', label: <FormattedMessage id="activities.frontoffice.stencil.title" /> })
    }
  },
  {
    id: "wrench",
    title: "activities.frontoffice.wrench.title",
    content: {
      label: "activities.frontoffice.wrench.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'wrench', label: <FormattedMessage id="activities.frontoffice.wrench.title" /> })
    }
  },
  {
    id: "dialob",
    title: "activities.frontoffice.dialob.title",
    content: {
      label: "activities.frontoffice.dialob.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'dialob', label: <FormattedMessage id="activities.frontoffice.dialob.title" /> })
    }
  },
  {
    id: "reporting",
    title: "activities.frontoffice.reporting.title",
    content: {
      label: "activities.frontoffice.reporting.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'reporting', label: <FormattedMessage id="activities.frontoffice.reporting.title" /> })
    }
  },
  {
    id: "userProfile",
    title: "activities.frontoffice.userProfile.title",
    content: {
      label: "activities.frontoffice.userProfile.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'userProfile', label: <FormattedMessage id="activities.frontoffice.userProfile.title" /> })
    }
  },
  {
    id: "allUserProfiles",
    title: "activities.frontoffice.allUserProfiles.title",
    content: {
      label: "activities.frontoffice.allUserProfiles.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'allUserProfiles', label: <FormattedMessage id="activities.frontoffice.allUserProfiles.title" /> })
    }
  },
  {
    id: "accessManagement",
    title: "activities.frontoffice.accessManagement.title",
    content: {
      label: "activities.frontoffice.accessManagement.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'accessManagement', label: <FormattedMessage id="activities.frontoffice.accessManagement.title" /> })
    }
  }


]);

//card view for all CREATE views
const Activities: React.FC<{}> = () => {
  const { actions } = Burger.useTabs();
  const cards = React.useMemo(() => createActivities({ actions }), [actions]);

  return (<StyledCards title="activities.title" desc="activities.desc" items={cards} />);
}

export { Activities };
