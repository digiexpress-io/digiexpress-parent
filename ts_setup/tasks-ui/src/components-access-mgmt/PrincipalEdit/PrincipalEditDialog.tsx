import React from 'react';
import Burger from 'components-burger';
import Backend from 'descriptor-backend';

import { StyledFullScreenDialog } from 'components-generic';
import { Left } from './Left';
import { Right } from './Right';
import Header from './Header';
import { PrincipalEditProvider, usePrincipalEdit } from './PrincipalEditContext';
import { ImmutableAmStore, Principal, useAm } from 'descriptor-access-mgmt';

const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { reload } = useAm();
  const backend = Backend.useBackend();
  const { entity, getCommands } = usePrincipalEdit();
  const commands = getCommands();

  const disabled = !entity.commitComment || !entity.email || !entity.name;


  async function handlePrincipalChange() {
    const { id } = entity;

    await new ImmutableAmStore(backend.store).updatePrincipal(id, commands);
    await reload();
    onClose();
  };
  return (
    <>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
      <Burger.PrimaryButton label='buttons.accept' disabled={disabled} onClick={handlePrincipalChange} />
    </>
  )
}

const PrincipalEditDialog: React.FC<{ open: boolean, onClose: () => void, principal: Principal }> = ({ open, onClose, principal }) => {
  if (!open) {
    return null;
  }


  return (
    <PrincipalEditProvider principal={principal}>
      <StyledFullScreenDialog
        open={open}
        onClose={onClose}
        header={<Header onClose={onClose} />}
        footer={<Footer onClose={onClose} />}
        left={<Left />}
        right={<Right />}
      />
    </PrincipalEditProvider>
  )
}

export { PrincipalEditDialog };