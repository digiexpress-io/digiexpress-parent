import React from 'react';
import { Box, Card, CardHeader, CardContent, CardActions, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { EveliActivities, EveliPermissions, EveliPermissionType } from '@dxs-ts/eveli-primitives';


import { FlowComposer } from '../wrench-flow';
import { DecisionComposer } from '../wrench-decision';
import { ServiceComposer } from '../wrench-service';
import { useWrenchNav } from '../wrench-nav';

import MigrationComposer from '../wrench-migration';



const ActivitiesViewItem: React.FC<{ data: ActivityProps }> = (props) => {
  const [open, setOpen] = React.useState<boolean>(false);
  const handleClose = () => setOpen(false);
  const handleOpen = () => setOpen(true);

  const Composer: React.FC<{ onClose: () => void }> = open === false ? () => (<></>) : props.data.composer;


  return (
    <>
      <Composer onClose={handleClose} />
      <Card>
        <CardHeader title={props.data.title} />
        <CardContent>{props.data.desc}</CardContent>

        <CardActions>
          <EveliPermissions id={props.data.permissionTypeView}>
            {props.data.buttonViewAll && props.data.onView ? <Button variant='text' onClick={props.data.onView} children={props.data.buttonViewAll} /> : <Box />}
          </EveliPermissions>

          <EveliPermissions id={props.data.permissionTypeCreate}>
            <Button onClick={handleOpen} children={props.data.buttonCreate} />
          </EveliPermissions>
        </CardActions>

      </Card >
    </>
  )
}

export interface ActivityProps {
  title: React.ReactNode;
  desc: React.ReactNode;
  permissionTypeCreate: EveliPermissionType;
  permissionTypeView: EveliPermissionType;
  buttonCreate: React.ReactNode;
  buttonViewAll?: React.ReactNode;
  onView?: () => void;
  composer: React.FC<{ onClose: () => void }>;
}

export function useActivities(): ActivityProps[] {
  const nav = useWrenchNav();
  return ([
    {
      composer: FlowComposer,
      onView: undefined,
      title: <FormattedMessage id="activities.flows.title" />,
      desc: <FormattedMessage id="activities.flows.desc" />,
      buttonCreate: <FormattedMessage id="buttons.create" />,
      permissionTypeCreate: 'CREATE_WRENCH_ASSET',
      permissionTypeView: 'NAV_TO_WRENCH_FLOWS'
    },
    {
      composer: DecisionComposer,
      onView: undefined,
      title: <FormattedMessage id="activities.decisions.title" />,
      desc: <FormattedMessage id="activities.decisions.desc" />,
      buttonCreate: <FormattedMessage id="buttons.create" />,
      buttonViewAll: undefined,
      permissionTypeCreate: 'CREATE_WRENCH_ASSET',
      permissionTypeView: 'NAV_TO_WRENCH_DECISIONS'
    },
    {
      composer: ServiceComposer,
      onView: undefined,
      title: <FormattedMessage id="activities.services.title" />,
      desc: <FormattedMessage id="activities.services.desc" />,
      buttonCreate: <FormattedMessage id="buttons.create" />,
      buttonViewAll: undefined,
      permissionTypeCreate: 'CREATE_WRENCH_ASSET',
      permissionTypeView: 'NAV_TO_WRENCH_SERVICES'
    },
    {
      composer: () => {
        const nav = useWrenchNav();
        React.useLayoutEffect(() => {
          nav.onNav({ type: 'DEBUG' });
        }, []);
        return (<></>);
      },      
      onView: undefined,
      title: <FormattedMessage id="activities.debug.title" />,
      desc: <FormattedMessage id="activities.debug.desc" />,
      buttonCreate: <FormattedMessage id="activities.debug.view" />,
      buttonViewAll: undefined,
      permissionTypeCreate: 'NAV_TO_WRENCH_DEBUG',
      permissionTypeView: 'NAV_TO_WRENCH_DEBUG'
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
      title: <FormattedMessage id="activities.compare.title" />,
      desc: <FormattedMessage id="activities.compare.desc" />,
      buttonCreate: <FormattedMessage id="activities.compare.view" />,
      buttonViewAll: undefined,
      permissionTypeCreate: 'NAV_TO_WRENCH_COMPARE',
      permissionTypeView: 'NAV_TO_WRENCH_COMPARE'
    },
    {
      composer: MigrationComposer,
      onView: undefined,
      title: <FormattedMessage id="activities.migration.title" />,
      desc: <FormattedMessage id="activities.migration.desc" />,
      buttonCreate: <FormattedMessage id="buttons.create" />,
      buttonViewAll: undefined,
      permissionTypeCreate: 'CREATE_WRENCH_ASSET',
      permissionTypeView: 'NAV_TO_WRENCH_MIGRATIONS'
    },
  ]);
}

//card view for all CREATE views
export const Activities: React.FC<{}> = () => {
  const activities = useActivities();
  return (
    <EveliActivities>
      {activities.map((card, index) => (<ActivitiesViewItem key={index} data={card} />))}
    </EveliActivities>
  );
}

