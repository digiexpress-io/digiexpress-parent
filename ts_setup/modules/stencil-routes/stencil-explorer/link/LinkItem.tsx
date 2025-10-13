import React from "react";

import { Box, Tooltip, useTheme } from '@mui/material';
import { Construction as ConstructionIcon } from '@mui/icons-material';
import { Link as LinkIcon } from '@mui/icons-material';
import { FormattedMessage, useIntl } from 'react-intl';


import { EveliPermissions, TreeItem } from "@dxs-ts/eveli-primitives";
import { StencilApi, StencilComposerApi as Composer } from '@dxs-ts/stencil-api';

import { LinkOptions } from './LinkOptions';
import ArticleItem from '../article/ArticleItem';





const LinkItem: React.FC<{ linkId: StencilApi.LinkId }> = ({ linkId }) => {
  const { session } = Composer.useComposer();
  const view = session.getLinkView(linkId);
  const { link } = view;
  const intl = useIntl();
  const theme = useTheme();

  const workflowName = session.getLinkName(link.id);
  const iconStyle = { mx: 0.5, color: theme.palette.primary.dark, fontSize: 'medium' };

  return (
    <>
      <TreeItem
        itemId={link.id}
        labelcolor="explorerItem"
        labelIcon={LinkIcon}
        labelText={
          <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
            {workflowName.name}
            {link.body.devMode && (
              <Tooltip title={intl.formatMessage({ id: 'services.devmode.tooltip' })}>
                <ConstructionIcon sx={iconStyle} />
              </Tooltip>
            )}
          </Box>
        }
      >
        <EveliPermissions id='EDIT_STENCIL_ASSET'>
          <TreeItem itemId={link.id + 'options-nested'} labelText={<FormattedMessage id="options" />}>
            <LinkOptions link={link} />
          </TreeItem>
        </EveliPermissions>


        {/** Article options */}
        <TreeItem itemId={link.id + 'articles-nested'}
          labelText={<FormattedMessage id="articles" />}
          labelInfo={`${link.body.articles.length}`}
          labelcolor="primary">

          {link.body.articles.map((id => session.getArticleView(id))).map(view => (
            <ArticleItem key={view.article.id} articleId={view.article.id} nodeId={`${link.id}-${view.article.id}-nested`} />
          ))}
        </TreeItem>

      </TreeItem>
    </>)
}

export default LinkItem;
