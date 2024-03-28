import React from 'react';
import { Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { StyledFullScreenDialog } from 'components-generic';
import Burger from 'components-burger';
import { Fields } from './PermissionCreateFields';
//import RoleCreateTabNavLoader from './RoleCreateTabNavigation';


const Left: React.FC<{}> = () => {
  return (
    <>
      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.permission.name' /></Typography>
        <Fields.PermissionName />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.permission.description' /></Typography>
        <Fields.PermissionDescription />
      </Burger.Section>
    </>)
}
/*
const Right: React.FC<{}> = () => {
  return (<RoleCreateTabNavLoader />)
}
*/

const Header: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  return (
    <Box display='flex' alignItems='center'>
      <Typography variant='h4'><FormattedMessage id='permissions.permission.create' /></Typography>
      <Box flexGrow={1} />
      <Fields.CloseDialogButton onClose={onClose} />
    </Box>
  )
}

const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
      <Burger.PrimaryButton label='buttons.accept' onClick={onClose} />
    </>
  )
}

const PermissionCreateDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ open, onClose }) => {
  return (
    <StyledFullScreenDialog
      open={open}
      onClose={onClose}
      header={<Header onClose={onClose} />}
      footer={<Footer onClose={onClose} />}
      left={<Left />}
      right={<>RIGHT</>}
    />
  )
}

export { PermissionCreateDialog };