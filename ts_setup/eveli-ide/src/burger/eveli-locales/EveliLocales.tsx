import React from 'react';
import { useThemeProps, Button, Popover, List, ListItem, ListItemButton, ListItemIcon } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { useAnchor } from './useAnchor';
import { MUI_NAME, EveliLocalesRoot, useUtilityClasses } from './useUtilityClasses';
import { EveliOverridableComponent } from '../api-variants';


export interface EveliLocalesProps {
  value?: string; // en, fi, sv
  locales?: string[]; //en, fi, sv
  hidden?: boolean;
  onClick?: (newLocale: string) => void;
  component?: EveliOverridableComponent<EveliLocalesProps>;
}

export const EveliLocales: React.FC<EveliLocalesProps> = (initProps) => {
  const { anchorProps, onClick: anchorOnClick } = useAnchor();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const ownerState = {
    ...props
  }

  const { value, onClick, 
    locales = ['en', 'fi', 'sv'], 
    hidden } = props;
  if (locales.length <= 1 && hidden) {
    return (<></>);
  }

  /**
   *  Map locales to country codes to get flag
  */
  const localeToCountryCode: Record<string, string> = {
    en: 'gb', // Great Britain English for correct flag
    fi: 'fi',
    sv: 'se'
  };

  function handleChange(newLocale: string) {
    onClick ? onClick(newLocale) : null;
  }

  const classes = useUtilityClasses();
  const startIcon = <img src={value ? `https://flagcdn.com/w20/${localeToCountryCode[value.toLowerCase()]}.png` : ''} />;

  const Root = props.component ?? EveliLocalesRoot;

  return (
    <Root ownerState={ownerState} className={classes.root}>
      
      <Button onClick={anchorOnClick}
        variant='text'
        startIcon={startIcon}
        className={classes.selectedLocale}>
        <FormattedMessage id={`locale.${value}`} />
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
                <FormattedMessage id={`locale.${value}`} />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Popover>

    </Root>);
}



