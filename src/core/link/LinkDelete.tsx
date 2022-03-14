import React from 'react';
import { useSnackbar } from 'notistack';

import { DialogContentText } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { Composer, StencilClient } from '../context';
import StencilStyles from '../styles';


interface LinkDeleteProps {
  linkId: StencilClient.LinkId,
  onClose: () => void,
}

const LinkDelete: React.FC<LinkDeleteProps> = ({ linkId, onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions } = Composer.useComposer();

  const handleDelete = () => {
    service.delete().link(linkId).then(success => {
      enqueueSnackbar(message, { variant: 'warning' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    })
  }
  const message = <FormattedMessage id="snack.link.deletedMessage" />

  return (
    <StencilStyles.Dialog open={true} onClose={onClose}
      backgroundColor="uiElements.main"
      title="link.delete.title"
      submit={{ title: "link.delete.title", onClick: handleDelete, disabled: false }}>
      <DialogContentText>
        <FormattedMessage id="link.delete" />
      </DialogContentText>
    </StencilStyles.Dialog>
  );
}
export { LinkDelete }
