import React from 'react';
import { useThemeProps } from '@mui/material';
import { useIntl } from 'react-intl';
import { useNavigate } from '@tanstack/react-router';

import { SiteApi } from '../api-site';
import { GLinks } from '../g-links';
import { useGArticleLinks } from './useGArticleLinks';
import { GLinkFormSecured, GLinkHyper, GLinkPhone, GLinkInfo, GLinkFormUnsecured } from '../g-link';
import { GLinksPageRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';



export interface GLinksPageProps {
  children: SiteApi.TopicView | undefined;
  component?: React.ElementType<GLinksPageProps>;
}


export const GLinksPage: React.FC<GLinksPageProps> = (props) => {
  const { formLinks, hyperlinks, phoneLinks, infoLinks } = useGArticleLinks(props.children);
  const intl = useIntl();
  const nav = useNavigate();

  const themeProps = useThemeProps({
    props,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(themeProps);

  function handleSecureLink(pageId: string, productId: string) {
    nav({
      params: { productId, pageId, locale: intl.locale },
      to: '/secured/$locale/pages/$pageId/products/$productId',
    })
  }


  function handleUnSecureLink(pageId: string, productId: string) {
    nav({
      params: { productId, pageId, locale: intl.locale },
      to: '/public/$locale/pages/$pageId/products/$productId',
    })
  }

  return (
    <GLinksPageRoot className={classes.root} as={themeProps.component}>
      {formLinks.length ?
        <GLinks header={intl.formatMessage({ id: 'gamut.article.pagelinks.forms.title' })}>
          {formLinks.map((formLink) => (formLink.secured ?
            <GLinkFormSecured key={formLink.id} onClick={() => handleSecureLink(props.children!.id, formLink.id)} label={formLink.name} value={formLink.value} /> :
            <GLinkFormUnsecured key={formLink.id} onClick={() => handleUnSecureLink(props.children!.id, formLink.id)} label={formLink.name} value={formLink.value} />
          ))}
        </GLinks> : <></>
      }

      {hyperlinks.length || phoneLinks.length || infoLinks.length ?
        <GLinks header={intl.formatMessage({ id: 'gamut.article.pagelinks.otherlinks.title' })}>
          {hyperlinks.map(link => <GLinkHyper key={link.id} label={link.name} value={link.value} />)}
          {phoneLinks.map(link => <GLinkPhone key={link.id} label={link.name} value={link.value} />)}
          {infoLinks.map(link => <GLinkInfo key={link.id} label={link.name} value={link.value} />)}

          <GLinkInfo label='Info link and stuff'
            value='Here is the content for the info link Here is the content for the info link Here is the content for the info link' />
        </GLinks> : <></>
      }
    </GLinksPageRoot>);
}