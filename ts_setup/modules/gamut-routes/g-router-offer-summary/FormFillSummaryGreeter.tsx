import { useIam, useOffers, useSite } from '@dxs-ts/gamut-api';
import React from 'react';
import { FormFillSummaryBoxAnon } from './FormFillSummaryBoxAnon';
import { FormFillSummaryBox } from './FormFillSummaryBox';
import { useNavigate } from '@tanstack/react-router';


interface FormFillSummaryGreeterProps {
  pageId: string;
  productId: string;
  locale: string;
}
export const FormFillSummaryGreeter: React.FC<FormFillSummaryGreeterProps> = ({ pageId, productId, locale }) => {
  const anon = useIam();
  const site = useSite();
  const nav = useNavigate();
  const { refresh, createOfferRps } = useOffers();

  const [rating, setRating] = React.useState<number | undefined>(undefined);
  const [comment, setComment] = React.useState('');

  const anonymousUser = anon.authType === 'ANON';
  const topic = site.views[pageId];
  const topicLink = topic.links.find(l => l.id === productId)
  const buttonBackToMsg = anonymousUser ? 'gamut.public.forms.summary.button.backToServicesHome' : 'gamut.forms.summary.button.back-to-overview';

  function navBack() {
    if (anonymousUser) {
      nav({
        from: '/public/$locale/pages/$pageId/products/$productId/offers/$offerId/summary',
        params: { locale },
        to: '/public/$locale'
      })
    }
    else {
      refresh();
      nav({
        from: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary',
        params: { viewId: 'user-overview' },
        to: '/secured/$locale/views/$viewId',
      })
    }
  }

  function handleNav() {
    if (rating !== undefined) {
      createOfferRps({ productId, rating, locale, comment: comment });
    }
    navBack();
  }

  if (anonymousUser) {
    return (
      <FormFillSummaryBoxAnon
        topicLink={topicLink}
        buttonBackToMsg={buttonBackToMsg}
        productId={productId}
        rating={rating}
        comment={comment}
        onNav={handleNav}
        onRatingChange={setRating}
        onCommentChange={setComment}
      />
    )
  }
  return (
    <FormFillSummaryBox
      topicLink={topicLink}
      buttonBackToMsg={buttonBackToMsg}
      onNav={handleNav}
      rating={rating}
      onRatingChange={setRating}
      comment={comment}
      onCommentChange={setComment}
    />
  )
}
