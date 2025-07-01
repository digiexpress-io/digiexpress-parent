import React from 'react';
import { generateUtilityClass, Typography, styled, Button } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { useNavigate } from '@tanstack/react-router';

import { GLogo } from '../g-logo';
import { useLocale } from '../api-locale';
import { useIam } from '../api-iam';
import { GUserOverviewMenuView } from '../g-user-overview-menu';



const MUI_NAME = 'GErrorNotFound';

export const GErrorNotFound: React.FC = () => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const nav = useNavigate();
  const { locale } = useLocale();

  const { authType } = useIam();

  function handleToHomePage(locale: string) {
    if (authType === 'ANON') {
      nav({
        params: { locale },
        to: '/public/$locale',
      })
    } else {
      const viewId: GUserOverviewMenuView = 'user-overview';
      nav({
        params: { locale, viewId },
        to: '/secured/$locale/views/$viewId',
      })
    }
  }

  return (
    <GErrorNotFoundRoot className={classes.root}>
      <div>
        <div className='logoAlign'>
          <GLogo variant='black_lg' />
        </div>
        <Typography>
          {intl.formatMessage({ id: 'gamut.error.notFound', defaultMessage: 'Ooops, the thing you are looking for is not found!' })}
        </Typography>
      </div>

      <div>
        <Button variant='contained' onClick={() => handleToHomePage(locale)}>
          {intl.formatMessage({ id: 'gamut.buttons.error.backToHome', defaultMessage: 'Go back to our home page' })}
        </Button>
      </div>
    </GErrorNotFoundRoot>
  )
}




const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

const GErrorNotFoundRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: theme.spacing(3),
    height: '100vh',
    flexDirection: 'column',

    '& .logoAlign': {
      textAlign: 'center',
      marginBottom: theme.spacing(3)
    }
  };
});