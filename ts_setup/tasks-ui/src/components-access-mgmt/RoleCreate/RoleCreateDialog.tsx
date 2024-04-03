import React from 'react';
import Burger from 'components-burger';
import { RoleCreateProvider, useNewRole } from './RoleCreateContext';
import Context from 'context';

import { StyledDialogLarge } from '../Dialogs';
import Header from './Header';
import { Left } from './Left';
import { Right } from './Right';
import { CreateRole, ImmutableAccessMgmtStore } from 'descriptor-access-mgmt';

const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { reload } = Context.useAccessMgmt();


  const backend = Context.useBackend();
  const { entity } = useNewRole();

  async function handleRoleCreate() {
    const { name, description, commitComment, parentId, permissions } = entity;
    const command: CreateRole = {
      commandType: 'CREATE_ROLE',
      name,
      description,
      parentId,
      comment: commitComment,
      permissions: [...permissions]
    }
    await new ImmutableAccessMgmtStore(backend.store).createRole(command);
    await reload();
    onClose();
  }

  return (
    <>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
      <Burger.PrimaryButton label='buttons.accept' onClick={handleRoleCreate} />
    </>
  )
}

const RoleCreateDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ open, onClose }) => {

  return (
    <RoleCreateProvider>
      <StyledDialogLarge
        open={open}
        onClose={onClose}
        header={<Header onClose={onClose} />}
        footer={<Footer onClose={onClose} />}
        left={<Left />}
        right={<Right />}
      />
    </RoleCreateProvider>
  )
}

export default RoleCreateDialog;