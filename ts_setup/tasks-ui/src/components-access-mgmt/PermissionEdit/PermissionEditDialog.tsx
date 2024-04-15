import React from 'react';
import Burger from 'components-burger';
import { ImmutableAmStore, Permission, useAm } from 'descriptor-access-mgmt';
import { StyledDialogLarge } from 'components-access-mgmt/Dialogs';
import { Left } from './Left';
import { PermissionEditProvider, usePermissionEdit } from './PermissionEditContext';
import { useBackend } from 'descriptor-backend';


const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { reload } = useAm();
  const { entity, getCommands } = usePermissionEdit();
  const backend = useBackend();

  const commands = getCommands();

  async function handlePermissionChange() {
    const { id } = entity;

    await new ImmutableAmStore(backend.store).updatePermission(id, commands);
    await reload();
    onClose();
  };
  return (
    <>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
      <Burger.PrimaryButton label='buttons.accept' onClick={handlePermissionChange} disabled={commands.length === 0 || !entity.commitComment} />
    </>
  )
}

const PermissionEditDialog: React.FC<{ open: boolean, onClose: () => void, permission: Permission }> = ({ open, onClose, permission }) => {

  if (!open) {
    return null;
  }

  return (
    <PermissionEditProvider permission={permission}>
      <StyledDialogLarge
        open={open}
        onClose={onClose}
        header={<>HEADER</>}
        footer={<Footer onClose={onClose} />}
        right={<>RIGHT</>}
        left={<Left />}
      />
    </PermissionEditProvider>
  )
}

export { PermissionEditDialog };