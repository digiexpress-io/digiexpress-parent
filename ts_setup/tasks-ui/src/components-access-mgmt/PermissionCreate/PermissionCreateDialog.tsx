import React from 'react';
import Burger from 'components-burger';
import { CreatePermission, ImmutableAccessMgmtStore } from 'descriptor-access-mgmt';
import Context from 'context';
import { StyledDialogLarge } from 'components-access-mgmt/Dialogs';
import { Header } from './Header';
import { Left } from './Left';
import { Right } from './Right';


const Footer: React.FC<{ onClose: () => void, onCloseCreate: () => void }> = ({ onClose, onCloseCreate }) => {

  return (
    <>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
      <Burger.PrimaryButton label='buttons.accept' onClick={onCloseCreate} />
    </>
  )
}
//TODO: When permission ctx is done
const PermissionCreateDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ open, onClose }) => {
  const backend = Context.useBackend();
  const permissions = Context.useAccessMgmt();
  const [name, setName] = React.useState('permission name');
  const [description, setDescription] = React.useState('description');
  const [comment, setComment] = React.useState('comment value');


  if (!open) {
    return null;
  }

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
    await new ImmutableAccessMgmtStore(backend.store).createPermission(command);
    handleCloseCreate();
  };


  return (
    <StyledDialogLarge
      open={open}
      onClose={onClose}
      header={<Header onClose={onClose} />}
      footer={<Footer onClose={onClose} onCloseCreate={handlePermissionCreate} />}
      right={<Right />}
      left={<Left />}
    />
  )
}

export { PermissionCreateDialog };