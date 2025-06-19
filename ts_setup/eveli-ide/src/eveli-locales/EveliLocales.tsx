import React from 'react';
import { useThemeProps, Typography, IconButton, Divider, MenuList, ListItemText, MenuItem } from '@mui/material';
import LanguageIcon from '@mui/icons-material/Language';

import { FormattedMessage } from 'react-intl';

import { useAnchor } from './useAnchor';
import { MUI_NAME, EveliLocalesRoot, EveliLocalesLanguageSelect, useUtilityClasses } from './useUtilityClasses';
import { EveliOverridableComponent } from '../api-variants';
import { useLocation, useNavigate, useParams } from '@tanstack/react-router';
import { useLocale } from '@/api-locale';


export interface EveliLocalesProps {
  value?: string; // en, fi, sv
  locales?: string[]; //en, fi, sv
  hidden?: boolean;
  onClick?: (newLocale: string) => void;
  component?: EveliOverridableComponent<EveliLocalesProps>;
}

export const EveliLocales: React.FC<EveliLocalesProps> = (initProps) => {
  const loc = useLocation();
  const navigate = useNavigate();
  const params = useParams({ from: '/secured/$locale' });
  const { setLocale } = useLocale();

  const { anchorProps, onClick: anchorOnClick, onClose: anchorOnClose } = useAnchor();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const ownerState = {
    ...props
  }

  const { 
    value = params.locale, 
    onClick, 
    locales = ['en', 'fi', 'sv'], 
    hidden } = props;

  if (locales.length <= 1 && hidden) {
    return (<></>);
  }


  function handleChange(locale: string) {
    const newPath = loc.pathname.replace(`/${params.locale}/`, `/{$locale}/`);
    navigate({
      to: newPath,
      params: (params: any) => {
        return { ...params, locale };
      },
      search: (prev: any) => {
        return prev;
      }
    });
    setLocale(locale);
    onClick ? onClick(locale) : null;
  }

  const classes = useUtilityClasses();
  const Root = props.component ?? EveliLocalesRoot;

  return (

    <Root ownerState={ownerState} className={classes.root}>
      <EveliLocalesLanguageSelect {...anchorProps}>
        <Typography><FormattedMessage id='menu.locales' /></Typography>
        <Divider />
        
        <MenuList dense>
          {locales.map((locale) => (
            <MenuItem key={locale} onClick={() => {
              handleChange(locale);
              anchorOnClose();
            }}>
              <ListItemText><FormattedMessage id={`locale.${locale}`} defaultMessage={locale}/></ListItemText>
            </MenuItem>
          ))}
        </MenuList>
      </EveliLocalesLanguageSelect>
      <IconButton onClick={anchorOnClick}><LanguageIcon /></IconButton>
      <Typography><FormattedMessage id={`locale.${value}`} /></Typography>
    </Root>
  );
}



