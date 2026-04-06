import React from 'react';
import { useSnackbar } from 'notistack';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography, Box } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';

import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { LinkDeleteRoot, useLinkDeleteUtilityClasses } from './useUtilityClasses';

interface LinkDeleteProps {
  linkId: StencilApi.LinkId;
  onClose: () => void;
}

export const LinkDelete: React.FC<LinkDeleteProps> = ({ linkId, onClose }) => {
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site } = Composer.useComposer();
  const classes = useLinkDeleteUtilityClasses();

  const link = site?.articleLinks?.[linkId];
  const linkType = link?.body?.contentType;
  const linkValue = link?.body?.value ?? '';

  const typeLabel =
    linkType === 'internal'
      ? intl.formatMessage({ id: 'link.type.internal' })
      : linkType === 'external'
      ? intl.formatMessage({ id: 'link.type.external' })
      : linkType === 'phone'
      ? intl.formatMessage({ id: 'link.type.phone' })
      : intl.formatMessage({ id: 'link.type.unknown' });

  const handleDelete = () => {
    service
      .delete()
      .link(linkId)
      .then((success) => {
        enqueueSnackbar(<FormattedMessage id="snack.link.deletedMessage" />, { variant: 'warning' });
        console.log(success);
        onClose();
        actions.handleLoadSite();
      });
  };

  return (
    <Dialog open onClose={onClose}>
      <DialogTitle>
        <FormattedMessage id="link.delete.title" />
      </DialogTitle>
  
      <DialogContent>
        <Typography sx={{ mb: 1 }}>
          <FormattedMessage id="link.delete" />
        </Typography>
        
        <LinkDeleteRoot className={classes.root}>
          <Box className={classes.infoBox}>
            <Typography variant="body2" component="div">
              <strong className={classes.label}>
                {intl.formatMessage({ id: 'link.type' })}:
              </strong>{' '}
              <span className={classes.value}>{typeLabel}</span>
            </Typography>
  
            <Typography variant="body2" component="div">
              <strong className={classes.label}>
                {intl.formatMessage({ id: 'link.value' })}:
              </strong>{' '}
              <span className={classes.value}>
                {linkValue || intl.formatMessage({ id: 'link.value.empty' })}
              </span>
            </Typography>
          </Box>
        </LinkDeleteRoot>
      </DialogContent>
  
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleDelete} color="error">
          <FormattedMessage id="button.delete.link" />
        </Button>
      </DialogActions>
    </Dialog>
  );
};
