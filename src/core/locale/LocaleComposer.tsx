import React from 'react';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';

import { Composer, StencilClient } from '../context';
import Burger from '@the-wrench-io/react-burger';

const LocaleComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site } = Composer.useComposer();
  const [locale, setLocale] = React.useState("");

  const handleCreate = () => {
    const entity: StencilClient.CreateLocale = { locale };
    console.log("entity", entity)
    service.create().locale(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    });
  }

  const message = <FormattedMessage id="snack.locale.createdMessage" />
  const locales: StencilClient.Locale[] = Object.values(site.locales).map(l => l.body.value);
  
  return (
    <Burger.Dialog open={true} onClose={onClose}
      backgroundColor="uiElements.main"
      title="locale.composer.title"
      submit={{ title: "button.create", onClick: handleCreate, disabled: !locale || locales.includes(locale) || locale.length !== 2 }}>

      <Burger.TextField label='locale.composer.placeholder' helperText='locale.composer.helper' placeholder="en"
        required
        value={locale}
        onChange={setLocale}
      />
    </Burger.Dialog>
  );
}

export { LocaleComposer }

