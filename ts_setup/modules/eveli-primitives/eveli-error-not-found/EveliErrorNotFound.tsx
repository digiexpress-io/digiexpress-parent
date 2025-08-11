import React from 'react';
import { Typography, styled, Button, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { useNavigate } from '@tanstack/react-router';

import { useLocale } from '@dxs-ts/eveli-api';
import { EveliLogo } from '../eveli-logo';


const MUI_NAME = 'EveliErrorNotFound';

export const EveliErrorNotFound: React.FC = () => {
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
    <EveliErrorNotFoundRoot className={classes.root}>
      <div>
        <div className="logoAlign">
          <EveliLogo variant="black_lg" />
        </div>
        <Typography variant="h6" align="center">
          {intl.formatMessage({
            id: 'eveli.error.notFound',
            defaultMessage: 'Ooops, the thing you are looking for is not found!',
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
    </EveliErrorNotFoundRoot>
  );
};

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

const EveliErrorNotFoundRoot = styled('div', {
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
