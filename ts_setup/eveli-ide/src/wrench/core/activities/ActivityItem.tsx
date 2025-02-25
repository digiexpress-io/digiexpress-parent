import React from 'react';
import { SxProps } from '@mui/system';
import { Card, CardHeader, CardActions, CardContent, Typography, Box, Divider, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

interface ActivityData {
  title: string;
  desc: string;
  buttonCreate: string;
  buttonViewAll?: string;
  buttonTertiary?: string;
  onView?: () => void;
  //onTertiary?: () => void;
}

const cardStyle: SxProps = {
  margin: 3,
  width: '20vw',
  display: 'flex',
  flexDirection: 'column',
};

const ActivityItem: React.FC<{ data: ActivityData, onCreate: () => void }> = (props) => {
  const title = (<Box display="flex" sx={{ justifyContent: 'center' }}>
    <Typography variant="h2" sx={{ fontWeight: 'bold', p: 1 }}><FormattedMessage id={props.data.title} /></Typography>
  </Box>);

  return (<Card sx={cardStyle}>
    <CardHeader title={title} />
    <CardContent sx={{ flexGrow: 1, p: 2, height: 'fit-content' }}>
      <Typography color="mainContent.contrastText" variant="body2"><FormattedMessage id={props.data.desc} /></Typography>
    </CardContent>

    <Divider />

    <CardActions sx={{ alignSelf: "flex-end" }}>
      <Box display="flex">
        {props.data.buttonViewAll && props.data.onView ? <Button variant='text' onClick={props.data.onView} children={<FormattedMessage id={props.data.buttonViewAll}/>} /> : <Box />}
        <Button onClick={props.onCreate} children={<FormattedMessage id={props.data.buttonCreate}/>}/>
      </Box>
    </CardActions>
  </Card>
  )
}
export type { ActivityData }
export { ActivityItem }
