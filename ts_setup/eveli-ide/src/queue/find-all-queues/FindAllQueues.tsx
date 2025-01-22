import React from 'react';
import { Box, Divider, List, ListItem, ListItemButton, Typography, useTheme } from '@mui/material';
import { useIntl } from 'react-intl';
import { useQueue, QueueApi } from '../queue-api';



export interface FindAllQueuesProps { }

export const FindAllQueues: React.FC<FindAllQueuesProps> = () => {
  const intl = useIntl();
  const theme = useTheme();
  const { getOneChannelConfig } = useQueue();

  const [config, setConfig] = React.useState<QueueApi.ChannelConfig>();

  React.useEffect(() => {
    getOneChannelConfig().then(setConfig)
  }, []);

  return (
    <Box padding={theme.spacing(3)}>
      <Typography variant='h1'>{intl.formatMessage({ id: 'queues.all' })}</Typography>
      <List dense disablePadding>
        {config ? config.queues.map((queue) => (
          <React.Fragment key={queue.id}>
            <ListItem dense disableGutters>
              <ListItemButton>
                <Box display='flex' gap={3} width='100%'>
                  <Box width='13%'>
                    <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'queue.config.queueName' })}</Typography>
                    <Typography>{queue.queueName}</Typography>
                  </Box>
                  <Box width='35%'>
                    <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'queue.config.createdAt' })}</Typography>
                    <Typography>{queue.createdAt}</Typography>
                  </Box>
                  <Box width='13%'>
                    <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'queue.config.createdBy' })}</Typography>
                    <Typography>{queue.createdBy}</Typography>
                  </Box>
                  <Box width='35%'>
                    <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'queue.config.comment' })}</Typography>
                    <Typography>{queue.comment}</Typography>
                  </Box>
                </Box>
              </ListItemButton>
            </ListItem>
            <Divider />
          </React.Fragment>
        )) : <>{intl.formatMessage({ id: 'queue.config.none' })}</>}
      </List>

      <Typography variant='h1'>{intl.formatMessage({ id: 'consumers.all' })}</Typography>
      <List dense disablePadding>
        {config ? config.queueConsumers.map((consumer) => (
          <React.Fragment key={consumer.id}>
            <ListItem dense disableGutters>
              <ListItemButton>
                <Box display='flex' gap={3} width='100%'>
                  <Box width='13%'>
                    <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'queue.config.routingKey' })}</Typography>
                    <Typography>{consumer.routingKey}</Typography>
                  </Box>
                  <Box width='10%'>
                    <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'queue.config.appId' })}</Typography>
                    <Typography>{consumer.appId}</Typography>
                  </Box>
                  <Box width='20%'>
                    <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'queue.config.consumerName' })}</Typography>
                    <Typography>{consumer.consumerName}</Typography>
                  </Box>
                  <Box width='13%'>
                    <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'queue.config.consumerStatus' })}</Typography>
                    <Typography>{consumer.consumerStatus}</Typography>
                  </Box>
                  <Box width='13%'>
                    <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'queue.config.comment' })}</Typography>
                    <Typography style={{wordBreak: "break-word"}}>{consumer.comment}</Typography>
                  </Box>
                  <Box width='13%'>
                    <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'queue.config.qualifiedJavaName' })}</Typography>
                    <Typography style={{wordBreak: "break-all"}}>{consumer.qualifiedJavaName}</Typography>
                  </Box>
                </Box>
              </ListItemButton>
            </ListItem>
            <Divider />
          </React.Fragment>
        )) : <>{intl.formatMessage({ id: 'queue.config.none' })}</>}
      </List>

    </Box>

  )
}