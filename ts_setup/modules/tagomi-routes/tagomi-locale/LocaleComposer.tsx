import React from 'react';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';

import { TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';
import { TagomiApi } from '@dxs-ts/tagomi-api';
import * as Burger from '@dxs-ts/eveli-primitives';
import { CancelButton } from '@dxs-ts/eveli-primitives';

const LocaleComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { backend, actions, site } = Composer.useComposer();
  const [locale, setLocale] = React.useState<string>("");

  const message = <FormattedMessage id="snack.locale.createdMessage" />
  const locales: TagomiApi.Locale[] = Object.values(site.locales);

  const handleCreate = () => {
    const entity: TagomiApi.CreateLocale = { localeCode: locale };
    console.log("entity", entity)
    backend.createLocale(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    });
  }

  const createDisabled =
    !locale
    || locales.some(l => l.localeCode === locale)
    || locale.length !== 2

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='tagomi.locale.composer.title' /></DialogTitle>
      <DialogContent>
        <Burger.TextField label='tagomi.locale.composer.placeholder' helperText='tagomi.locale.composer.helper' placeholder="en"
          required
          value={locale}
          onChange={setLocale}
        />
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleCreate} disabled={createDisabled
        }>
          <FormattedMessage id='button.create' />
        </Button>
      </DialogActions>
    </Dialog>);
}

export { LocaleComposer }

