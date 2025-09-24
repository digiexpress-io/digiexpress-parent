import React from 'react';
import { useThemeProps, Button, Popover, List, ListItem, ListItemButton, ListItemIcon } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';

import { useAnchor } from './useAnchor';
import { MUI_NAME, GLocalesRoot, useUtilityClasses } from './useUtilityClasses';
import { GOverridableComponent } from '@dxs-ts/gamut-api';

export interface GLocalesProps {
  value?: string; // current locale, e.g. en, fi, sv
  locales?: string[]; // available locales, e.g. ['en', 'fi', 'sv', 'my', 'bs']
  localeToCountryCode?: Record<string, string>;
  hidden?: boolean;
  onClick?: (newLocale: string) => void;
  component?: GOverridableComponent<GLocalesProps>;
  showOnlyFlag?: boolean;
}

export const GLocales: React.FC<GLocalesProps> = (initProps) => {
  const intl = useIntl();
  const { anchorProps, onClick: anchorOnClick } = useAnchor();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const ownerState = { ...props };

  const { value, onClick, locales = [], hidden } = props;
  if (locales.length <= 1 && hidden) {
    return (<></>);
  }

  /**
   *  Map locales to country codes to get flag
   */
  const localeToCountryCode: Record<string, string> = {
    en: 'gb', // Great Britain flag for English
    fi: 'fi',
    sv: 'se',
    my: 'my',
    bs: 'ba',
    ...(props.localeToCountryCode ?? {})
  };

  function handleChange(newLocale: string) {
    onClick ? onClick(newLocale) : null;
  }

  const classes = useUtilityClasses();
  const startIcon = (
    <img src={value ? `https://flagcdn.com/w20/${localeToCountryCode[value.toLowerCase()]}.png` : ''} />
  );

  const Root = props.component ?? GLocalesRoot;

  // Analyzer-hint: makes sure intl analyzer sees all supported keys
  if (false) {
    intl.formatMessage({ id: 'gamut.locale.en' });
    intl.formatMessage({ id: 'gamut.locale.fi' });
    intl.formatMessage({ id: 'gamut.locale.sv' });
    intl.formatMessage({ id: 'gamut.locale.my' });
    intl.formatMessage({ id: 'gamut.locale.bs' });
  }

  return (
    <Root ownerState={ownerState} className={classes.root}>
      <Button
        onClick={anchorOnClick}
        variant='text'
        startIcon={startIcon}
        className={classes.selectedLocale}
      >
        {!props.showOnlyFlag && value && (
          <FormattedMessage id={"gamut.locale." + value} />
        )}
      </Button>
      <Popover {...anchorProps}>
        <List disablePadding>
          {locales.map((locale) => (
            <ListItem key={locale} disablePadding>
              <ListItemButton
                onClick={() => {
                  handleChange(locale);
                  anchorProps.onClose();
                }}
              >
                <ListItemIcon>
                  <img
                    src={`https://flagcdn.com/w20/${localeToCountryCode[locale.toLowerCase()]}.png`}
                  />
                </ListItemIcon>
                <FormattedMessage id={"gamut.locale." + locale} />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Popover>
    </Root>
  );
};
