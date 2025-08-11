import React from 'react';
import { Button } from '@mui/material';
import PersonOutlinedIcon from '@mui/icons-material/PersonOutlined';
import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';

import { useOffers } from '@dxs-ts/gamut-api';
import { GAuthFormStart } from '@dxs-ts/gamut-primitives';

import { GRouterProductButtons, useUtilityClasses } from './useUtilityClasses';
import { GRouterProductOwnerState } from './g-router-product-types'


export const GRouterProductActions: React.FC<GRouterProductOwnerState> = (props) => {
  const nav = useNavigate();
  const offers = useOffers();
  const intl = useIntl();

  const { topicLink, topic, locale, isAnon, status } = props.ownerState;
  const productId = topicLink?.id;
  const classes = useUtilityClasses();
  // article links
  const parentPageId = topic.parent?.id ?? undefined;
  const pageId = topic.id;



  function handleCancelOffer() {
    if (isAnon) {
      nav({
        from: '/public/$locale/pages/$pageId/products/$productId',
        params: { locale },
        to: '/public/$locale'
      })
    } else {
      nav({
        from: '/secured/$locale/pages/$pageId/products/$productId',
        params: { viewId: 'user-overview' },
        to: '/secured/$locale/views/$viewId',
      })
    }
  }

  function handleCreateOffer() {
    if (!productId) {
      return;
    }

    offers.createOffer({ locale, productId, parentPageId, pageId }).then((offer) => {
      if (isAnon) {
        nav({
          params: { locale, pageId, productId, offerId: offer.id },
          to: '/public/$locale/pages/$pageId/products/$productId/offers/$offerId',
        })
      } else {
        nav({
          params: { locale, pageId, productId, offerId: offer.id },
          to: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId',
        })
      }
    })
  }

  function handleAfterLogin() {
    nav({
      from: '/public/$locale/pages/$pageId/products/$productId',
      to: '/secured/$locale/pages/$pageId/products/$productId',
    })
  }

  return (
    <GRouterProductButtons className={classes.root}>
      <Button variant='outlined' onClick={handleCancelOffer}>
        {intl.formatMessage({ id: 'gamut.forms.filling.cancel.button' })}
      </Button>

      {(status === 'IS_USER_FORM_ENABLED' || status === 'IS_ANON_FORM_ENABLED' || status === 'IS_REP_ENABLED') && ( 
        <Button variant='contained' className={classes.formStartButton} onClick={handleCreateOffer}>
          {intl.formatMessage({ id: 'gamut.forms.filling.start.button' })}
        </Button>
      )}

      {status === 'IS_REP_DISABLED' || status === 'IS_ANON_FORM_DISABLED' && ( 
        <GAuthFormStart forced onSubmit={handleAfterLogin}>
          <Button className={classes.formAuthButton} type='submit' variant='contained' startIcon={<PersonOutlinedIcon />}>
            {intl.formatMessage({ id: 'gamut.forms.filling.login-then-start.button' })}
          </Button>
        </GAuthFormStart>
      )}
    </GRouterProductButtons>

  )
}

