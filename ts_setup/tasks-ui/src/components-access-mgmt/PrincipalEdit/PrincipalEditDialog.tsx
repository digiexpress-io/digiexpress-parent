import React from 'react';
import Burger from 'components-burger';
import Backend from 'descriptor-backend';

import { StyledDialogLarge } from '../Dialogs';

import { PrincipalEditProvider, usePrincipalEdit } from './PrincipalEditContext';
import { Principal, useAm } from 'descriptor-access-mgmt';

const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { reload } = useAm();
  const backend = Backend.useBackend();
  const { entity } = usePrincipalEdit();
  const disabled = !entity.commitComment || !entity.email || !entity.name;



  return (
    <>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
      <Burger.PrimaryButton label='buttons.accept' disabled={disabled} onClick={() => { }} />
    </>
  )
}

const PrincipalEditDialog: React.FC<{ open: boolean, onClose: () => void, principal: Principal }> = ({ open, onClose, principal }) => {
  if (!open) {
    return null;
  }


  return (
    <PrincipalEditProvider principal={principal}>
      <StyledDialogLarge
        open={open}
        onClose={onClose}
        header={<>{principal.name}</>}
        footer={<Footer onClose={onClose} />}
        left={<></>}
        right={<></>}
      />
    </PrincipalEditProvider>
  )
}

export { PrincipalEditDialog };