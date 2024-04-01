import React from 'react';
import { Box, Typography, TextField } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';

import { StyledFullScreenDialog } from 'components-generic';
import Burger from 'components-burger';
import { CreatePermission, ImmutablePermissionStore } from 'descriptor-permissions';
import { Fields } from './PermissionCreateFields';

import Context from 'context';


/*  TODO
const Left: React.FC<{}> = () => {
  return (<Fields.PermissionsCreateFields />)
}
*/

const Right: React.FC<{}> = () => {
  return (<>RIGHT</>);
}


const Header: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  return (
    <Box display='flex' alignItems='center'>
      <Typography variant='h4'><FormattedMessage id='permissions.permission.create' /></Typography>
      <Box flexGrow={1} />
      <Fields.CloseDialogButton onClose={onClose} />
    </Box>
  )
}

const Footer: React.FC<{ onClose: () => void, onCloseCreate: () => void }> = ({ onClose, onCloseCreate }) => {

  return (
    <>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
      <Burger.PrimaryButton label='buttons.accept' onClick={onCloseCreate} />
    </>
  )
}

const PermissionCreateDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ open, onClose }) => {
  const intl = useIntl();
  const backend = Context.useBackend();
  const permissions = Context.usePermissions();
  const [name, setName] = React.useState('permission name');
  const [description, setDescription] = React.useState('description');
  const [comment, setComment] = React.useState('comment value');

  function handleCloseCreate() {
    permissions.reload().then(() => {
      onClose();
    });
  }

  async function handlePermissionCreate() {
    const command: CreatePermission = {
      commandType: 'CREATE_PERMISSION',
      comment,
      name,
      description,
      roles: []
    };
    await new ImmutablePermissionStore(backend.store).createPermission(command);
    handleCloseCreate();
  };

  function handleNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.target.value);
  }

  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    setDescription(event.target.value);
  }

  function handleCommentChange(event: React.ChangeEvent<HTMLInputElement>) {
    setComment(event.target.value);
  }

  return (
    <StyledFullScreenDialog
      open={open}
      onClose={onClose}
      header={<Header onClose={onClose} />}
      footer={<Footer onClose={onClose} onCloseCreate={handlePermissionCreate} />}
      right={<Right />}
      left={
        <>
          <Burger.Section>
            <Typography fontWeight='bold'><FormattedMessage id='permissions.permission.name' /></Typography>
            <TextField InputProps={{ disableUnderline: true }} variant='standard'
              placeholder={intl.formatMessage({ id: 'permissions.permission.name.create.placeholder' })}
              fullWidth
              value={name}
              onChange={handleNameChange}
            />
          </Burger.Section>

          <Burger.Section>
            <Typography fontWeight='bold'><FormattedMessage id='permissions.permission.description' /></Typography>
            <TextField InputProps={{ disableUnderline: true }} variant='standard'
              placeholder={intl.formatMessage({ id: 'permissions.permission.description.create.placeholder' })}
              fullWidth
              multiline
              minRows={3}
              maxRows={6}
              value={description}
              onChange={handleDescriptionChange}
            />
          </Burger.Section>

          <Burger.Section>
            <Typography fontWeight='bold'><FormattedMessage id='permissions.permission.createComment' /></Typography>
            <TextField InputProps={{ disableUnderline: true }} variant='standard'
              placeholder={intl.formatMessage({ id: 'permissions.permission.createComment.placeholder' })}
              fullWidth
              required
              value={comment}
              onChange={handleCommentChange}
            />
          </Burger.Section>
        </>
      }
    />
  )
}

export { PermissionCreateDialog };