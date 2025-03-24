import React from "react";
import ConstructionIcon from '@mui/icons-material/Construction';

import { FormattedMessage } from 'react-intl';

import * as Burger from '@/eveli-styles';
import { StencilComposerApi as Composer } from '../../stencil-setup';
import { StencilApi } from '@/api-stencil';

import { LinkOptions } from './LinkOptions';
import ArticleItem from '../article/ArticleItem';

const LinkItem: React.FC<{ linkId: StencilApi.LinkId }> = ({ linkId }) => {
  const { session } = Composer.useComposer();
  const view = session.getLinkView(linkId);
  const { link } = view;


  const workflowName = session.getLinkName(link.id);

  return (
    <>
      <Burger.TreeItem
        itemId={link.id}
        labelText={workflowName.name}
        labelcolor="explorerItem"
        labelIcon={link.body.devMode ? ConstructionIcon : undefined}
        >

        <Burger.TreeItem itemId={link.id + 'options-nested'} labelText={<FormattedMessage id="options" />}>
          <LinkOptions link={link} />
        </Burger.TreeItem>


        {/** Article options */}
        <Burger.TreeItem itemId={link.id + 'articles-nested'}
          labelText={<FormattedMessage id="articles" />}
          labelInfo={`${link.body.articles.length}`}
          labelcolor="primary">

          {link.body.articles.map((id => session.getArticleView(id))).map(view => (
            <ArticleItem key={view.article.id} articleId={view.article.id} nodeId={`${link.id}-${view.article.id}-nested`}/>
          ))}
        </Burger.TreeItem>

      </Burger.TreeItem>
    </>)
}

export default LinkItem;
