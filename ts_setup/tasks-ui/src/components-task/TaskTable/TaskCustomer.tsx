import React from 'react';


import { Avatar, Box, Typography } from '@mui/material';
import { TaskDescriptor } from 'descriptor-task';
import { CustomerDescriptor, ImmutableCustomerDescriptor, ImmutableCustomerStore } from 'descriptor-customer';
import Backend from 'descriptor-backend';
import { FormattedMessage } from 'react-intl';
import { useAvatar } from 'descriptor-avatar';


export function useCustomer(customerId: string | undefined) {
  const avatar = useAvatar(customerId!);
  const backend = Backend.useBackend();
  const [customer, setCustomer] = React.useState<CustomerDescriptor>();

  React.useEffect(() => {
    if (!customerId) {
      return;
    }
    new ImmutableCustomerStore(backend.store).getCustomer(customerId)
      .then(customer => setCustomer(new ImmutableCustomerDescriptor(customer)));
  }, [customerId]);

  return { avatar, customer }
}

export const TaskCustomer: React.FC<{ task: TaskDescriptor }> = ({ task }) => {
  const { customerId } = task;
  const { customer, avatar } = useCustomer(customerId);

  if (!customer) {
    return <>...</>;
  }

  return (
    <Box display="flex" flexDirection="row">
      <Box pl={1} pr={2}>
        <Avatar sx={{ bgcolor: avatar?.colorCode, width: 48, height: 48, fontSize: 20 }}>{avatar?.letterCode}</Avatar>
      </Box>
      <Box display="flex" flexDirection="column">
        <Typography variant='h5'>{customer.displayName}</Typography>
        <Box sx={{ pt: 1 }} />
        <Typography variant='caption'>
          <FormattedMessage id='core.teamSpace.customerId' values={{ customerId: customer.entry.externalId }} />
        </Typography>
      </Box>
    </Box>
  )
}
