import React from 'react';
import { DialogContentText } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { useSnackbar } from 'notistack';

import { Composer, StencilClient } from '../context';
import Burger from '@the-wrench-io/react-burger';


interface ReleaseDeleteProps {
  id: StencilClient.ReleaseId;
  onClose: () => void;
}

const ReleaseDelete: React.FC<ReleaseDeleteProps> = ({ id, onClose }) => {
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const message = <FormattedMessage id="snack.release.deletedMessage" />

  const handleDelete = () => {
    service.delete().release(id).then(_success => {
      enqueueSnackbar(message, {variant: 'warning'});
      onClose();
      actions.handleLoadSite();
    });
  }

  return (
    <Burger.Dialog open={true} onClose={onClose}
      backgroundColor="uiElements.main"
      title="release.delete.title"
      submit={{ title: "button.delete", onClick: handleDelete, disabled: false }}>
      <DialogContentText>
        <FormattedMessage id="release.delete.desc" />
      </DialogContentText>
    </Burger.Dialog>);
};
export { ReleaseDelete };
