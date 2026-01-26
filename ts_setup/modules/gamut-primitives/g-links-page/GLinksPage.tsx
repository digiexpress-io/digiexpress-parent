import React from 'react';
import { useThemeProps } from '@mui/material';
import { useIntl } from 'react-intl';
import { useNavigate } from '@tanstack/react-router';

import { SiteApi, useSite } from '@dxs-ts/gamut-api';
import { GLinks } from '../g-links';
import { useGArticleLinks } from './useGArticleLinks';
import { GLinkFormLocked, GLinkHyper, GLinkPhone, GLinkInfo, GLinkFormUnlocked, GLinkArticle } from '../g-link';
import { GLinksPageRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { useIam } from '@dxs-ts/gamut-api';


export interface GLinksPageProps {
  children: SiteApi.TopicView;
  component?: React.ElementType<GLinksPageProps>;
  viewId?: string | undefined;
}


export const GLinksPage: React.FC<GLinksPageProps> = (props) => {
  const { formLinks, hyperlinks, phoneLinks, infoLinks } = useGArticleLinks(props.children);
  const intl = useIntl();
  const nav = useNavigate();
  const anon = useIam();
  const loggedIn = anon.authType !== 'ANON';
  const { topics } = useSite();

  const childTopics = (props.children.children ?? []).flatMap(child => {
    const topic = topics.find(topic => topic.id === child.id);
    return topic ? [topic] : [];
  });

  const themeProps = useThemeProps({
    props,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(themeProps);

  function handleForm(form: SiteApi.TopicLink) {
    const pageId: string = props.children.id
    const productId: string = form.id;


    if (loggedIn) {
      nav({
        params: { productId, pageId, locale: intl.locale },
        to: '/secured/$locale/pages/$pageId/products/$productId',
      })
    } else {
      nav({
        params: { productId, pageId, locale: intl.locale },
        to: '/public/$locale/pages/$pageId/products/$productId',
      })
    }
  }

  function handleTopicChange(topic: SiteApi.TopicView) {
    if (loggedIn) {
      nav({
        from: '/secured/$locale/views/$viewId',
        params: { viewId: 'services' },
        search: { topicId: topic.id },
        to: '/secured/$locale/views/$viewId',
      })
    } else {
      nav({
        from: '/public/$locale',
        params: { locale: intl.locale, pageId: topic.id },
        to: '/public/$locale/pages/$pageId',
      })
    }
  }

  /*
  1. Unsecured site=true + secured=false + anon=true
  2. Secured site=true + secured=true
  */
  return (
    <GLinksPageRoot className={classes.root} as={themeProps.component}>
      {formLinks.length ?
        <GLinks header={intl.formatMessage({ id: 'gamut.article.pagelinks.forms.title' })}>
          {formLinks.map((formLink) => (anon.isFormLinkEnabled(formLink) ?
            <GLinkFormUnlocked key={formLink.id} onClick={() => handleForm(formLink)} label={formLink.name} value={formLink.value} /> :
            <GLinkFormLocked key={formLink.id} onClick={() => handleForm(formLink)} label={formLink.name} value={formLink.value} /> 
          ))}
        </GLinks> : <></>
      }

      {hyperlinks.length || phoneLinks.length ?
        <GLinks header={intl.formatMessage({ id: 'gamut.article.pagelinks.otherlinks.title' })}>
          {hyperlinks.map(link => <GLinkHyper key={link.id} label={link.name} value={link.value} />)}
          {phoneLinks.map(link => <GLinkPhone key={link.id} label={link.name} value={link.value} />)}
        </GLinks> : <></>
      }

      {infoLinks.length ? infoLinks.map(link =>
        <GLinks key={link.id} header={link.name}>
          <GLinkInfo label={undefined} value={link.value.replace('<info>', '')} />
        </GLinks>) : <></>}

      {childTopics.length ?
        <GLinks header={intl.formatMessage({ id: 'gamut.article.childtopics.title', defaultMessage: 'Pages' })}>
          {childTopics.map(child => <GLinkArticle key={child.id} label={child.name} value={child.name} onClick={() => handleTopicChange(child)} />)}
        </GLinks> : <></>
      }
    </GLinksPageRoot>);
}