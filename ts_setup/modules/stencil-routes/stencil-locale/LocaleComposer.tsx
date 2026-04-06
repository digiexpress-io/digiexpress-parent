import React from 'react';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';

import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import * as Burger from '@dxs-ts/eveli-primitives';
import { CancelButton } from '@dxs-ts/eveli-primitives';

const LocaleComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site } = Composer.useComposer();
  const [locale, setLocale] = React.useState("");


  const message = <FormattedMessage id="snack.locale.createdMessage" />
  const locales: StencilApi.Locale[] = Object.values(site.locales).map(l => l.body.value);

  const handleCreate = () => {
    const entity: StencilApi.CreateLocale = { locale: locale.trim() };
    console.log("entity", entity)
    service.create().locale(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    });
  }

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='locale.composer.title' /></DialogTitle>
      <DialogContent>
        <Burger.TextField label='locale.composer.placeholder' helperText='locale.composer.helper' placeholder="en"
          required
          value={locale}
          onChange={setLocale}
        />
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleCreate} disabled={!locale.trim() || locales.includes(locale) || locale.trim().length !== 2}>
          <FormattedMessage id='button.create'/>
        </Button>
      </DialogActions>
    </Dialog>);
}

export { LocaleComposer }

