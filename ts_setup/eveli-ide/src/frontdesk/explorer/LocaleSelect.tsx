import * as React from 'react';
import { Menu, MenuItem, Button, Typography, useTheme } from '@mui/material';
import { useLocaleSelect } from '../context';
import { frontdeskIntl } from '../intl';
import { useIntl } from 'react-intl';


export const LocaleSelect: React.FC = () => {
  const { locale, setLocale } = useLocaleSelect();
  const theme = useTheme();
  const intl = useIntl();
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

  const availableLocales = Object.keys(frontdeskIntl);

  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLElement, MouseEvent>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleSelect = (newValue: string) => {
    setLocale(newValue)
    console.log(newValue)
    setAnchorEl(null);
  };


  return (
    <div>
      <Button variant='text' onClick={handleClick}>
        <Typography sx={{
          textTransform: 'uppercase',
          fontWeight: 'bold',
          color: theme.palette.explorerItem.main
        }}
        >
          {locale}
        </Typography>
      </Button>

      <Menu anchorEl={anchorEl} open={open} onClose={handleSelect}>
        {availableLocales.map((value) => (
          <MenuItem
            onClick={() => handleSelect(value)}
            disabled={locale === value}
          >
            {intl.formatMessage({ id: `locale.${value}` })}
          </MenuItem>
        ))}
      </Menu>
    </div>
  );
}