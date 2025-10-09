import React from 'react';

import { Card, CardHeader, CardActions, CardContent, Box, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { EveliPermissions, EveliPermissionType, EveliActivities } from '@dxs-ts/eveli-primitives';

import { useTagomiNav } from '../tagomi-nav';



export interface ActivityProps {
  title: React.ReactNode;
  desc: React.ReactNode;
  buttonCreate: React.ReactNode;
  buttonViewAll?: React.ReactNode;
  permissionTypeCreate: EveliPermissionType;
  permissionTypeView: EveliPermissionType;
  onView?: () => void;
  composer: React.FC<{ onClose: () => void }>;
}

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
      </Card>
    </>
  )
}

export function useActivities(): ActivityProps[] {
  const nav = useTagomiNav();
  return ([])
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
