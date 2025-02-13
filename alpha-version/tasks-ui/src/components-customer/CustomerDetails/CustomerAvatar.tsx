import React from 'react';


import { Avatar, Box, Button, Typography } from '@mui/material';
import { TaskDescriptor } from 'descriptor-task';
import { CustomerDescriptor, ImmutableCustomerDescriptor, ImmutableCustomerStore } from 'descriptor-customer';
import Backend from 'descriptor-backend';
import { FormattedMessage } from 'react-intl';
import { useAvatar } from 'descriptor-avatar';
import { useToggle } from 'components-generic';
import { CustomerDetailsDialog } from './CustomerDetailsDialog';


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

export const CustomerAvatar: React.FC<{ customerId?: string }> = ({ customerId }) => {
  const { customer, avatar } = useCustomer(customerId);
  const editCustomer = useToggle();

  if (!customer) {
    return <>...</>;
  }

  return (<>
    <CustomerDetailsDialog open={editCustomer.open} onClose={editCustomer.handleEnd} customer={customerId} />
    <Button variant="text" onClick={editCustomer.handleStart}>
      <Box display="flex" flexDirection="row">
        <Box pl={1} pr={2}>
          <Avatar sx={{ bgcolor: avatar?.colorCode, width: 48, height: 48, fontSize: 20 }}>{avatar?.letterCode}</Avatar>
        </Box>
        <Box display="flex" flexDirection="column">
          <Typography variant='h5'>{customer.displayName}</Typography>
          <Box sx={{ pt: 1 }} />
          <Typography variant='caption' sx={{ alignSelf: "self-start" }}>
            <FormattedMessage id='core.teamSpace.customerId' values={{ customerId: customer.entry.externalId }} />
          </Typography>
        </Box>
      </Box>
    </Button>
    </>
  )
}
