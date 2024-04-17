import React from 'react';
import Burger from 'components-burger';
import { ImmutableAmStore, Role, useAm } from 'descriptor-access-mgmt';
import { StyledDialogLarge } from 'components-access-mgmt/Dialogs';
import { RoleEditProvider, useRoleEdit } from './RoleEditContext';
import { useBackend } from 'descriptor-backend';


const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { reload } = useAm();
  const { entity, getCommands } = useRoleEdit();
  const backend = useBackend();
  const commands = getCommands();

  async function handleRoleChange() {
    const { id } = entity;

    await new ImmutableAmStore(backend.store).updateRole(id, commands);
    await reload();
    onClose();
  };
  return (
    <>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
      <Burger.PrimaryButton label='buttons.accept' onClick={handleRoleChange} disabled={commands.length === 0 || !entity.commitComment} />
    </>
  )
}

const RoleEditDialog: React.FC<{ open: boolean, onClose: () => void, role: Role }> = ({ open, onClose, role }) => {

  if (!open) {
    return null;
  }

  return (
    <RoleEditProvider role={role}>
      <StyledDialogLarge
        open={open}
        onClose={onClose}
        header={<></>}
        footer={< Footer onClose={onClose} />}
        right={<></>}
        left={<></>}
      />
    </RoleEditProvider>
  )
}

export { RoleEditDialog };