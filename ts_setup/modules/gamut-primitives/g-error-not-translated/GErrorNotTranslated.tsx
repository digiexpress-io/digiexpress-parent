import React from 'react';
import { generateUtilityClass, Typography, styled, Button } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { useRouter } from '@tanstack/react-router';

import { GLogo } from '../g-logo';



const MUI_NAME = 'GErrorNotTranslated';

export const GErrorNotTranslated: React.FC = () => {
  const intl = useIntl();
  const router = useRouter();

  function handleBack() {
    router.history.back();
  }

  const classes = useUtilityClasses();

  return (
    <GErrorNotTranslatedRoot className={classes.root}>
      <div>
        <div className='logoAlign'>
          <GLogo variant='black_lg' />
        </div>
        <Typography>
          {intl.formatMessage({ id: 'gamut.error.notTranslated', defaultMessage: 'Sorry, this content has not been translated into your selected language' })}
        </Typography>
      </div>

      <div>
        <Button variant='contained' onClick={handleBack}>
          {intl.formatMessage({ id: 'gamut.buttons.error.back', defaultMessage: 'Go back' })}
        </Button>
      </div>
    </GErrorNotTranslatedRoot>
  )
}




const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

const GErrorNotTranslatedRoot = styled("div", {
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