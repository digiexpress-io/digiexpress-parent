import React from 'react';
import Burger from 'components-burger';
import { CreatePermission, ImmutableAmStore, useAm } from 'descriptor-access-mgmt';
import Backend from 'descriptor-backend';
import { StyledFullScreenDialog } from 'components-generic';
import { Header } from './Header';
import { Left } from './Left';
import { Right } from './Right';
import { PermissionCreateProvider, useNewPermission } from './PermissionCreateContext';


const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { reload } = useAm();
  const backend = Backend.useBackend();
  const { entity } = useNewPermission();
  const disabled = !entity.name || !entity.commitComment || !entity.description;

  async function handlePermissionCreate() {
    const { commitComment, description, name, roles, principals } = entity;

    const command: CreatePermission = {
      commandType: 'CREATE_PERMISSION',
      comment: commitComment,
      name,
      description,
      principals: [...principals],
      roles: [...roles]
    };
    await new ImmutableAmStore(backend.store).createPermission(command);
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
      <StyledFullScreenDialog
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