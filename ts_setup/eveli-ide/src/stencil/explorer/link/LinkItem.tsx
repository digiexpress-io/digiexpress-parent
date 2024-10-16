import React from "react";

import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';
import LinkIcon from '@mui/icons-material/Link';
import EditIcon from '@mui/icons-material/ModeEdit';
import ConstructionIcon from '@mui/icons-material/Construction';

import { FormattedMessage } from 'react-intl';

import * as Burger from '@/burger';
import { Composer, StencilApi } from '../../context';

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
        labelIcon={link.body.devMode ? ConstructionIcon : LinkIcon}
        >

        <Burger.TreeItem itemId={link.id + 'options-nested'} labelText={<FormattedMessage id="options" />} labelIcon={EditIcon}>
          <LinkOptions link={link} />
        </Burger.TreeItem>


        {/** Article options */}
        <Burger.TreeItem itemId={link.id + 'articles-nested'}
          labelText={<FormattedMessage id="articles" />}
          labelIcon={FolderOutlinedIcon}
          labelInfo={`${link.body.articles.length}`}
          labelcolor="article">

          {link.body.articles.map((id => session.getArticleView(id))).map(view => (
            <ArticleItem key={view.article.id} articleId={view.article.id} nodeId={`${link.id}-${view.article.id}-nested`}/>
          ))}
        </Burger.TreeItem>

      </Burger.TreeItem>
    </>)
}

export default LinkItem;
