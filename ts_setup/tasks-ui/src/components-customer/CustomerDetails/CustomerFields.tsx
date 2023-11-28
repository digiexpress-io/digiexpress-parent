import React from 'react';
import { Typography, IconButton, Stack, Grid } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage } from 'react-intl';
import { CustomerDescriptor } from 'descriptor-customer';

import Context from 'context';


const CustomerInfo: React.FC<{ customer: CustomerDescriptor }> = ({ customer }) => {

  return (
    <Stack direction='column' spacing={1}>
      <Grid container alignItems='center'>
        <Grid item md={3} lg={3}>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.firstName' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.lastName' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.ssn' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.type' /></Typography>
        </Grid>

        <Grid item md={9} lg={9} alignItems='center'>
          <Typography>{customer.toPerson().firstName}</Typography>
          <Typography>{customer.toPerson().lastName}</Typography>
          <Typography>{customer.entry.externalId}</Typography>
          <Typography><FormattedMessage id='customer.type.PERSON' /></Typography>
        </Grid>
      </Grid>
    </Stack>);
}

const CustomerContact: React.FC<{ customer: CustomerDescriptor }> = ({ customer }) => {

  return (
    <Stack direction='column' spacing={1}>
      <Grid container alignItems='center'>
        <Grid item md={3} lg={3}>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.email' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.street' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.locality' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.country' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.postalCode' /></Typography>
        </Grid>

        <Grid item md={9} lg={9} alignItems='center'>
          <Typography>{customer.toPerson().contact?.email}</Typography>
          <Typography>{customer.toPerson().contact?.address.street}</Typography>
          <Typography>{customer.toPerson().contact?.address.locality}</Typography>
          <Typography>{customer.toPerson().contact?.address.country}</Typography>
          <Typography>{customer.toPerson().contact?.address.postalCode}</Typography>
        </Grid>
      </Grid>
    </Stack>);
}

const CustomerTask: React.FC<{ customer: CustomerDescriptor }> = () => {

  return (
    <Stack direction='column' spacing={1}>
      <Grid container alignItems='center'>
        <Grid item md={3} lg={3}>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.task.name' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.task.dateOpened' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.task.dueDate' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.task.assignees' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.task.status' /></Typography>
        </Grid>

        <Grid item md={9} lg={9} alignItems='center'>
          <Typography>General message</Typography>
          <Typography>8/11/2023</Typography>
          <Typography>21/11/2023</Typography>
          <Typography>Lord Vetrinary, Lady Sybil Vimes</Typography>
          <Typography>CREATED</Typography>
        </Grid>
      </Grid>
    </Stack>);
}



const CustomerEvents: React.FC<{ customer: CustomerDescriptor }> = () => {

  return (
    <Stack direction='column' spacing={1}>
      <Grid container alignItems='center'>
        <Grid item md={4} lg={4}>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.lastLogin' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.formDeleted' /></Typography>
          <Typography fontWeight='bolder'><FormattedMessage id='customer.formCompleted' /></Typography>
        </Grid>

        <Grid item md={4} lg={4}>
          <Typography>09/12/2023, 17:30</Typography>
          <Typography>01/09/2023, 09:12</Typography>
          <Typography>12/12/2023, 11:51</Typography>
        </Grid>


        <Grid item md={4} lg={4} alignItems='center'>
          <Typography>Logged in to portal</Typography>
          <Typography>General Message</Typography>
          <Typography>Building permit</Typography>
        </Grid>
      </Grid>
    </Stack>);
}


const CloseDialogButton: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <IconButton onClick={onClose}>
      <CloseIcon />
    </IconButton>
  )
}


const Fields = { CustomerInfo, CustomerContact, CustomerTask, CustomerEvents, CloseDialogButton };
export default Fields;
