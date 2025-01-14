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
    page?: GArticleSlot,
    pageLinks?: GArticleSlot;
    pageBottom?: GArticleSlot;
    pageFeedback?: GArticleSlot;
  };
  component?: GOverridableComponent<GArticleProps>;
}

export type GArticleSlot = React.ElementType<{ children: SiteApi.TopicView | undefined }>;

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
  const Page: GArticleSlot = slots?.page ?? GPage;
  const PageLinks: GArticleSlot = slots?.pageLinks ?? GLinksPage;
  const PageBottom: GArticleSlot = slots?.pageBottom ?? (() => <div></div>);
  const PageFeedback: GArticleSlot = slots?.pageFeedback ?? GArticleFeedback;

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
