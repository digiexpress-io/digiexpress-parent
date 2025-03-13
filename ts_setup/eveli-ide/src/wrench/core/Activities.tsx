import React from 'react';
import { Box, Card, CardHeader, CardContent, CardActions, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import * as Burger from '@/burger'

import { FlowComposer } from './flow';
import { DecisionComposer } from './decision';
import { ServiceComposer } from './service';

import ReleaseComposer from './release';
import MigrationComposer from './migration';

import { useWrenchNav } from './nav';



const ActivitiesViewItem: React.FC<{ data: ActivityProps }> = (props) => {
  const [open, setOpen] = React.useState<boolean>(false);
  const handleClose = () => setOpen(false);
  const handleOpen = () => setOpen(true);

  const Composer: React.FC< {onClose: () => void}> = open === false ? () => (<></>) : props.data.composer;

  return (
    <>
    <Composer onClose={handleClose}/>
    <Card>
      <CardHeader title={props.data.title} />
      <CardContent>{props.data.desc}</CardContent>
      <CardActions>
        {props.data.buttonViewAll && props.data.onView ? <Button variant='text' onClick={props.data.onView} children={props.data.buttonViewAll} /> : <Box />}
        <Button onClick={handleOpen} children={props.data.buttonCreate}/>
      </CardActions>
    </Card>
    </>
  )
}

export interface ActivityProps {
  title: React.ReactNode;
  desc: React.ReactNode;
  buttonCreate: React.ReactNode;
  buttonViewAll?: React.ReactNode;
  onView?: () => void;
  composer: React.FC< {onClose: () => void}>;
}

export function useActivities(): ActivityProps[] {
  const nav = useWrenchNav();
  return ([
    {
      composer: FlowComposer,
      onView: undefined,
      title: <FormattedMessage id="activities.flows.title" />,
      desc: <FormattedMessage id="activities.flows.desc"/>,
      buttonCreate: <FormattedMessage id="buttons.create"/>,
    },
    {
      composer: DecisionComposer,
      onView: undefined,
      title: <FormattedMessage id="activities.decisions.title"/>,
      desc: <FormattedMessage id="activities.decisions.desc"/>,
      buttonCreate: <FormattedMessage id="buttons.create"/>,
      buttonViewAll: undefined
    },
    {
      composer: ServiceComposer,
      onView: undefined,
      title: <FormattedMessage id="activities.services.title"/>,
      desc: <FormattedMessage id="activities.services.desc"/>,
      buttonCreate: <FormattedMessage id="buttons.create"/>,
      buttonViewAll: undefined
    },
    {
      composer: () => {
        const nav = useWrenchNav();
        React.useLayoutEffect(() => {
          () => nav.onNav({ type: 'DEBUG' })
        }, [])

        return (<></>)
      },
      onView: undefined,
      title: <FormattedMessage id="activities.debug.title"/>,
      desc: <FormattedMessage id="activities.debug.desc"/>,
      buttonCreate: <FormattedMessage id="activities.debug.view"/>,
      buttonViewAll: undefined,
    },
    {
      composer: ReleaseComposer,
      onView: () => nav.onNav({ type: 'RELEASES' }),
      title: <FormattedMessage id="activities.releases.title"/>,
      desc: <FormattedMessage id="activities.releases.desc"/>,
      buttonCreate: <FormattedMessage id="buttons.create"/>,
      buttonViewAll: <FormattedMessage id="activities.releases.view"/>,
    },
    {
      composer: () => {
        const nav = useWrenchNav();
        React.useLayoutEffect(() => {
          nav.onNav({ type: 'COMPARE' })
        }, [])

        return (<></>)
      },
      onView: undefined,
      title: <FormattedMessage id="activities.compare.title"/>,
      desc: <FormattedMessage id="activities.compare.desc"/>,
      buttonCreate: <FormattedMessage id="activities.compare.view"/>,
      buttonViewAll: undefined,
    },
    {
      composer: MigrationComposer,
      onView: undefined,
      title: <FormattedMessage id="activities.migration.title"/>,
      desc: <FormattedMessage id="activities.migration.desc"/>,
      buttonCreate: <FormattedMessage id="buttons.create"/>,
      buttonViewAll: undefined
    },
  ]);
}

//card view for all CREATE views
export const Activities: React.FC<{}> = () => {
  const activities = useActivities();
  return (
    <Burger.EveliActivities>
      {activities.map((card, index) => (<ActivitiesViewItem key={index} data={card} />))}
    </Burger.EveliActivities>
  );
}

