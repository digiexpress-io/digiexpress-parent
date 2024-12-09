import React from 'react';
import { useThemeProps } from '@mui/material';
import { SiteApi } from '../api-site';
import { useUtilityClasses, GArticleRoot, MUI_NAME } from './useUtilityClasses';
import { GPage } from './GPage'
import { GLinksPage } from '../g-links-page'
import { GArticleFeedback } from '../g-article-feedback'
import { GOverridableComponent } from '../g-override'

export interface GArticleProps {
  children: SiteApi.TopicView | undefined;
  slots?: {
    page?: React.ElementType<{ children: SiteApi.TopicView | undefined }>,
    pageLinks?: React.ElementType<{ children: SiteApi.TopicView | undefined }>;
    pageBottom?: React.ElementType<{ children: SiteApi.TopicView | undefined }>;
    pageFeedback?: React.ElementType<{ children: SiteApi.TopicView | undefined }>;
  };
  component?: GOverridableComponent<GArticleProps>;
}


export const GArticle: React.FC<GArticleProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })

  const classes = useUtilityClasses(props);
  const slots = props.slots;
  const ownerState = {
    ...props,
    ...slots
  }

  const topic: SiteApi.TopicView | undefined = props.children;
  const Page: React.ElementType<{ children: SiteApi.TopicView | undefined }> = slots?.page ?? GPage;
  const PageLinks: React.ElementType<{ children: SiteApi.TopicView | undefined }> = slots?.pageLinks ?? GLinksPage;
  const PageBottom: React.ElementType<{ children: SiteApi.TopicView | undefined }> = slots?.pageBottom ?? (() => <div></div>);
  const PageFeedback: React.ElementType<{ children: SiteApi.TopicView | undefined }> = slots?.pageFeedback ?? GArticleFeedback;
  const Root = props.component ?? GArticleRoot;

  return (
    <Root ownerState={ownerState} className={classes.root}>
      
      <div className={classes.content}>
        <div className={classes.page}>
          <Page>{topic}</Page>
        </div>
        <div className={classes.pageLinks}>
          <PageLinks>{topic}</PageLinks>
        </div>
      </div>
      <div className={classes.pageBottom}>
        <PageBottom>{topic}</PageBottom>
        <PageFeedback>{topic}</PageFeedback>
      </div>
    </Root>)
}
