import React from 'react';
import { useThemeProps, Button, Popover, List, ListItem, ListItemButton, ListItemIcon } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { useAnchor } from './useAnchor';
import { MUI_NAME, GLocalesRoot, useUtilityClasses } from './useUtilityClasses';
import { GOverridableComponent } from '@dxs-ts/gamut-api';

export interface GLocalesProps {
  value?: string; // en, fi, sv, my, bs
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
  const ownerState = { ...props };

  const { value, onClick, hidden } = props;
  if (hidden) {
    return (<></>);
  }

  /**
   *  Fixed mapping of locales to country codes
   */
  const localeToCountryCode: Record<string, string> = {
    en: 'gb', // Great Britain flag for English
    fi: 'fi',
    sv: 'se',
    my: 'my',
    bs: 'ba',
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
        {!props.showOnlyFlag && value && (
          <>
            {value === 'en' && <FormattedMessage id="gamut.locale.en" />}
            {value === 'fi' && <FormattedMessage id="gamut.locale.fi" />}
            {value === 'sv' && <FormattedMessage id="gamut.locale.sv" />}
            {value === 'my' && <FormattedMessage id="gamut.locale.my" />}
            {value === 'bs' && <FormattedMessage id="gamut.locale.bs" />}
          </>
        )}
      </Button>
      <Popover {...anchorProps}>
        <List disablePadding>
          <ListItem disablePadding>
            <ListItemButton onClick={() => { handleChange('en'); anchorProps.onClose(); }}>
              <ListItemIcon><img src={`https://flagcdn.com/w20/gb.png`} /></ListItemIcon>
              <FormattedMessage id="gamut.locale.en" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton onClick={() => { handleChange('fi'); anchorProps.onClose(); }}>
              <ListItemIcon><img src={`https://flagcdn.com/w20/fi.png`} /></ListItemIcon>
              <FormattedMessage id="gamut.locale.fi" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton onClick={() => { handleChange('sv'); anchorProps.onClose(); }}>
              <ListItemIcon><img src={`https://flagcdn.com/w20/se.png`} /></ListItemIcon>
              <FormattedMessage id="gamut.locale.sv" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton onClick={() => { handleChange('my'); anchorProps.onClose(); }}>
              <ListItemIcon><img src={`https://flagcdn.com/w20/my.png`} /></ListItemIcon>
              <FormattedMessage id="gamut.locale.my" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton onClick={() => { handleChange('bs'); anchorProps.onClose(); }}>
              <ListItemIcon><img src={`https://flagcdn.com/w20/ba.png`} /></ListItemIcon>
              <FormattedMessage id="gamut.locale.bs" />
            </ListItemButton>
          </ListItem>
        </List>
      </Popover>
    </Root>
  );
};
