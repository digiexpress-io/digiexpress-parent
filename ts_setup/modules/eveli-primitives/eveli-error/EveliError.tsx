import React from 'react';
import { Typography, styled, Button, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { useNavigate } from '@tanstack/react-router';
import { useLocale } from '@dxs-ts/eveli-api';

import { EveliLogo } from '../eveli-logo';


const MUI_NAME = 'EveliError';

export const EveliError: React.FC = () => {
  const intl = useIntl();
  const navigate = useNavigate();

  let locale = 'en';
  try {
    // useLocale() might fail if the context hasn't been set up (e.g. during app crashes).
    // We catch and ignore errors here to ensure the error screen itself never crashes.
    locale = useLocale()?.locale || 'en';
  } catch {
    // Intentionally left blank: if useLocale() throws, we fall back to 'en' above to keep the error page stable.
  }  

  const handleGoHome = () => {
    navigate({
      to: '/secured/$locale',
      params: { locale },
    });
  };

  const classes = useUtilityClasses();

  return (
    <EveliErrorRoot className={classes.root}>
      <div>
        <div className="logoAlign">
          <EveliLogo variant="black_lg" />
        </div>
        <Typography variant="h6" align="center">
          {intl.formatMessage({
            id: 'eveli.error.general',
            defaultMessage: 'Ooops, we have encountered an error!',
          })}
        </Typography>
      </div>

      <div>
        <Button variant="contained" onClick={handleGoHome}>
          {intl.formatMessage({
            id: 'eveli.error.backToHome',
            defaultMessage: 'Go back to our home page',
          })}
        </Button>
      </div>
    </EveliErrorRoot>
  );
};

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

const EveliErrorRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => [styles.root],
})(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  flexDirection: 'column',
  height: '100vh',
  gap: theme.spacing(3),

  '& .logoAlign': {
    textAlign: 'center',
    marginBottom: theme.spacing(3),
  },
}));
