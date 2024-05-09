import React from 'react';


import { Avatar, Box, Button, Typography } from '@mui/material';
import { TaskDescriptor } from 'descriptor-task';
import { FormattedMessage } from 'react-intl';
import { useCustomer } from '../TaskTable';
import Customer from 'components-customer';



export const TaskCustomer: React.FC<{ task: TaskDescriptor }> = ({ task }) => {
  const { customerId } = task;
  const { customer, avatar } = useCustomer(customerId);
  const [edit, setEdit] = React.useState(false);

  function handleStartEdit() {
    setEdit(true);
  }

  function handleEndEdit() {
    setEdit(false);
  }
  if (!customer) {
    return <>...</>;
  }
//
  return (<>
    <Customer.CustomerDetailsDialog open={edit} onClose={handleEndEdit} customer={customerId} />
    <Box display="flex" flexDirection="row">
      <Box alignSelf="center">
        <Avatar sx={{ bgcolor: avatar?.colorCode, width: 24, height: 24, fontSize: 10 }}>{avatar?.letterCode}</Avatar>
      </Box>
      <Button onClick={handleStartEdit} variant="text"><b>{customer.displayName}, <FormattedMessage id='core.teamSpace.customerId' values={{ customerId: customer.entry.externalId }} /></b></Button>
    </Box>
    </>
  )
}
