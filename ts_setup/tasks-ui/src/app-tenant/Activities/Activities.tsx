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
    id: "dialob",
    title: "activities.frontoffice.dialob.title",
    content: {
      label: "activities.frontoffice.dialob.desc"
    },
    primary: {
      label: "buttons.view",
      onClick: () => actions.handleTabAdd({ id: 'dialob', label: <FormattedMessage id="activities.frontoffice.dialob.title" /> })
    }
  }

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
