import React from 'react';
import Burger from 'components-burger';
import { RoleCreateProvider } from './RoleCreateContext';
import Context from 'context';

import { StyledDialogLarge } from '../Dialogs';
import Header from './Header';
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

const RoleCreateDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ open, onClose }) => {
  const permissions = Context.usePermissions();

  function handleCloseCreate() {
    permissions.reload().then(() => {
      onClose();
    });
  }

  return (
    <RoleCreateProvider>
      <StyledDialogLarge
        open={open}
        onClose={onClose}
        header={<Header onClose={onClose} />}
        footer={<Footer onClose={onClose} onCloseCreate={handleCloseCreate} />}
        left={<Left />}
        right={<Right />}
      />
    </RoleCreateProvider>
  )
}

export default RoleCreateDialog;