import React from 'react';
import Burger from 'components-burger';
import Backend from 'descriptor-backend';

import { StyledFullScreenDialog } from 'components-generic';
import PlaybooksCreateHeader from './PlaybooksCreateHeader';
import { PlaybooksCreateLeft } from './PlaybooksCreateLeft';
import { PlaybooksCreateRight } from './PlaybooksCreateRight';
import { PlaybooksCreateProvider } from './PlaybooksCreateContext';


const PlaybooksCreateFooter: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  async function handleRoleCreate() {
    /*
    const { name, description, commitComment, parentId, permissions, principals } = entity;
    const command: CreateRole = {
      commandType: 'CREATE_ROLE',
      name,
      description,
      parentId,
      comment: commitComment,
      principals: [...principals],
      permissions: [...permissions]
    }
    await new ImmutableAmStore(backend.store).createRole(command);
    await reload();
    onClose();
    */
  }


  const disabled = false;

  return (
    <>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
      <Burger.PrimaryButton label='buttons.accept' disabled={disabled} onClick={handleRoleCreate} />
    </>
  )
}

const PlaybooksCreateDialob: React.FC<{ open: boolean, onClose: () => void }> = ({ open, onClose }) => {
  if (!open) {
    return null;
  }


  return (
    <PlaybooksCreateProvider>
      <StyledFullScreenDialog
        open={open}
        onClose={onClose}
        header={<PlaybooksCreateHeader onClose={onClose} />}
        footer={<PlaybooksCreateFooter onClose={onClose} />}
        left={<PlaybooksCreateLeft />}
        right={<PlaybooksCreateRight />}
      />
    </PlaybooksCreateProvider>
  )
}

export default PlaybooksCreateDialob;