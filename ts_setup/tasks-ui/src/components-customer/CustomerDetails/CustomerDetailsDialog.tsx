import React from 'react';
import { Box, Stack, Grid, Typography, CircularProgress } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { StyledFullScreenDialog } from 'components-generic';


import Context from 'context';
import Burger from 'components-burger';
import { ImmutableCustomerDescriptor, CustomerDescriptor, ImmutableCustomerStore } from 'descriptor-customer';


import Fields from './CustomerFields';


const Left: React.FC<{ customer: CustomerDescriptor }> = ({ customer }) => {
  return (
    <Stack spacing={1} direction='column' pt={1}>
      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='customer.details.customerInfo' /></Typography>
        <Fields.CustomerInfo customer={customer} />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='customer.contactInfo' /></Typography>
        <Fields.CustomerContact customer={customer} />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='customer.tasks' /></Typography>
        <Fields.CustomerTask customer={customer} />
      </Burger.Section>
    </Stack>
  )
}

const Right: React.FC<{ customer: CustomerDescriptor }> = ({ customer }) => {

  return (
    <Stack spacing={1} direction='column' pt={1}>
      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='customer.notifications.preferences' /></Typography>
        <Fields.CustomerNotificationPref />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='customer.events' /></Typography>
        <Fields.CustomerEvents customer={customer} />
      </Burger.Section>
    </Stack>
  );

}

const Header: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  return (
    <Grid container>
      <Grid item md={11} lg={11}>
        <FormattedMessage id='customer.details.dialog.title' />
      </Grid>
      <Box flexGrow={1} />
      <Grid item md={1} lg={1} sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', pb: 1 }}>
        <Fields.CloseDialogButton onClose={onClose} />
      </Grid>
    </Grid>
  )
}

const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <Burger.PrimaryButton label='buttons.close' onClick={onClose} />
  )
}

const CustomerDetailsDialog: React.FC<{ open: boolean, onClose: () => void, customer: string | undefined }> = (props) => {
  const tasks = Context.useTasks();
  const backend = Context.useBackend();
  const [customer, setCustomer] = React.useState<CustomerDescriptor>();

  React.useEffect(() => {
    const id = props.customer;
    if (!id) {
      return;
    }
    new ImmutableCustomerStore(backend.store).getCustomer(id).then(customer => setCustomer(new ImmutableCustomerDescriptor(customer)));
  }, [props.customer]);

  if (!props.open) {
    return null;
  }

  if (!customer) {
    return (<CircularProgress />);
  }

  function handleClose() {
    tasks.reload().then(() => props.onClose());
  }

  return (
    <StyledFullScreenDialog
      header={<Header onClose={props.onClose} />}
      footer={<Footer onClose={handleClose} />}
      left={<Left customer={customer} />}
      right={<Right customer={customer} />}
      onClose={props.onClose}
      open={props.open}
    />
  );
}

export default CustomerDetailsDialog;
