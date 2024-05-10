import React from 'react';


import { Avatar, Box } from '@mui/material';
import { TaskDescriptor } from 'descriptor-task';
import { FormattedMessage } from 'react-intl';
import { useCustomer } from 'components-customer';



export const TaskCustomer: React.FC<{ task: TaskDescriptor }> = ({ task }) => {
  const { customerId } = task;
  const { customer, avatar } = useCustomer(customerId);
  if (!customer) {
    return <>...</>;
  }
//
  return (<>
    <Box display="flex" flexDirection="row">
      <Box alignSelf="center" paddingRight={1}>
        <Avatar sx={{ bgcolor: avatar?.colorCode, width: 24, height: 24, fontSize: 10 }}>{avatar?.letterCode}</Avatar>
      </Box>
      {customer.displayName}, <FormattedMessage id='core.teamSpace.customerId' values={{ customerId: customer.entry.externalId }} />
    </Box>
    </>
  )
}
