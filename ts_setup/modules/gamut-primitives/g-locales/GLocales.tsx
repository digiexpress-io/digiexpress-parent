import React from 'react';
import { useThemeProps, Button, Popover, List, ListItem, ListItemButton, ListItemIcon } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { useAnchor } from './useAnchor';
import { MUI_NAME, GLocalesRoot, useUtilityClasses } from './useUtilityClasses';
import { GOverridableComponent } from '@dxs-ts/gamut-api';


export interface GLocalesProps {
  value?: string; // en, fi, sv
  locales?: string[]; //en, fi, sv
  localeToCountryCode?: Record<string, string>;
  hidden?: boolean;
  onClick?: (newLocale: string) => void;
  component?: GOverridableComponent<GLocalesProps>;
  showOnlyFlag?: boolean;
}

export const GLocales: React.FC<GLocalesProps> = (initProps) => {
  const { anchorProps, onClick: anchorOnClick } = useAnchor();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const ownerState = {
    ...props
  }

  const { value, onClick, locales = [], hidden } = props;
  if (locales.length <= 1 && hidden) {
    return (<></>);
  }

  /**
   *  Map locales to country codes to get flag
  */
  const localeToCountryCode: Record<string, string> = {
    en: 'gb', // Great Britain English for correct flag
    fi: 'fi',
    sv: 'se',
    et: 'ee',
    my: 'my',
    bs: 'ba',
    ...(props.localeToCountryCode ?? {})
  };

  function handleChange(newLocale: string) {
    onClick ? onClick(newLocale) : null;
  }

  const classes = useUtilityClasses();
  const startIcon = <img src={value ? `https://flagcdn.com/w20/${localeToCountryCode[value.toLowerCase()]}.png` : ''} />;

  const Root = props.component ?? GLocalesRoot;

  return (
    <Root ownerState={ownerState} className={classes.root}>
      <Button
        onClick={anchorOnClick}
        variant='text'
        startIcon={startIcon}
        className={classes.selectedLocale}
      >
        {!props.showOnlyFlag && <FormattedMessage id={"gamut.locale." + value} />}
      </Button>
      <Popover {...anchorProps}>
        <List disablePadding>
          {locales.map((locale) => (
            <ListItem key={locale} disablePadding>
              <ListItemButton onClick={() => {
                handleChange(locale);
                anchorProps.onClose();
              }}>
                <ListItemIcon><img src={`https://flagcdn.com/w20/${localeToCountryCode[locale.toLowerCase()]}.png`} /></ListItemIcon>
                <FormattedMessage id={"gamut.locale." + locale} />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Popover>

    </Root>);
}



