import React from 'react';
import Burger from 'components-burger';
import Context from 'context';

import { StyledDialogLarge } from '../Dialogs';
import Header from './Header';
import { Left } from './Left';
import { Right } from './Right';
import { PrincipalCreateProvider, useNewPrincipal } from './PrincipalCreateContext';
import { CreatePrincipal, ImmutableAccessMgmtStore } from 'descriptor-access-mgmt';

const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { reload } = Context.useAm();
  const backend = Context.useBackend();
  const { entity } = useNewPrincipal();

  const disabled = !entity.commitComment || !entity.email || !entity.username;

  async function handlePrincipalCreate() {
    const { username, email, commitComment, permissions, roles } = entity;
    const command: CreatePrincipal = {
      commandType: 'CREATE_PRINCIPAL',
      name: username,
      email,
      comment: commitComment,
      permissions: [...permissions],
      roles: [...roles],
    }
    await new ImmutableAccessMgmtStore(backend.store).createPrincipal(command);
    await reload();
    onClose();
  }

  return (
    <>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
      <Burger.PrimaryButton label='buttons.accept' disabled={disabled} onClick={handlePrincipalCreate} />
    </>
  )
}

const PrincipalCreateDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ open, onClose }) => {
  if (!open) {
    return null;
  }


  return (
    <PrincipalCreateProvider>
      <StyledDialogLarge
        open={open}
        onClose={onClose}
        header={<Header onClose={onClose} />}
        footer={<Footer onClose={onClose} />}
        left={<Left />}
        right={<Right />}
      />
    </PrincipalCreateProvider>
  )
}

export default PrincipalCreateDialog;