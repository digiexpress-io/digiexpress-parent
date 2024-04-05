import React from 'react';
import Burger from 'components-burger';
import { CreatePermission, ImmutableAccessMgmtStore } from 'descriptor-access-mgmt';
import Context from 'context';
import { StyledDialogLarge } from 'components-access-mgmt/Dialogs';
import { Header } from './Header';
import { Left } from './Left';
import { Right } from './Right';
import { PermissionCreateProvider, useNewPermission } from './PermissionCreateContext';


const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { reload } = Context.useAccessMgmt();
  const backend = Context.useBackend();
  const { entity } = useNewPermission();
  const disabled = !entity.name || !entity.commitComment || !entity.description;

  async function handlePermissionCreate() {
    const { commitComment, description, name, roles } = entity;
    const command: CreatePermission = {
      commandType: 'CREATE_PERMISSION',
      comment: commitComment,
      name,
      description,
      roles: [...roles]
    };
    await new ImmutableAccessMgmtStore(backend.store).createPermission(command);
    await reload();
    onClose();
  };

  return (
    <>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
      <Burger.PrimaryButton label='buttons.accept' onClick={handlePermissionCreate} disabled={disabled} />
    </>
  )
}

const PermissionCreateDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ open, onClose }) => {

  if (!open) {
    return null;
  }

  return (
    <PermissionCreateProvider>
      <StyledDialogLarge
        open={open}
        onClose={onClose}
        header={<Header onClose={onClose} />}
        footer={<Footer onClose={onClose} />}
        right={<Right />}
        left={<Left />}
      />
    </PermissionCreateProvider>
  )
}

export { PermissionCreateDialog };