import * as React from "react";
import { Box, Tooltip, Typography, useTheme } from "@mui/material";

import NotInterestedIcon from '@mui/icons-material/NotInterested';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import MenuBookOutlinedIcon from '@mui/icons-material/MenuBookOutlined';
import FaceIcon from '@mui/icons-material/Face';
import ConstructionIcon from '@mui/icons-material/Construction';

import { FormattedMessage, useIntl } from 'react-intl';


import { EveliPermissions, TreeItem, TreeItemRoot } from "@dxs-ts/eveli-primitives";
import { StencilApi, StencilComposerApi as Composer } from '@dxs-ts/stencil-api';

import { ArticleOptions } from './ArticleOptions';
import ArticlePageItem from './ArticlePageItem';


interface WorkflowItemProps {
  labelText: string;
  nodeId: string;
  children?: React.ReactNode;
  devMode?: boolean,
  disabledMode?: boolean,
  anonMode?: boolean,
  onClick: () => void;
}

const WorkflowItem: React.FC<WorkflowItemProps> = (props) => {
  const theme = useTheme();
  const intl = useIntl();

  const iconStyle = { mx: 0.5, color: theme.palette.primary.dark, fontSize: 'medium' };
  const noConfig = !props.devMode && !props.anonMode && !props.disabledMode;


  return (
    <TreeItemRoot
      itemId={props.nodeId}
      onClick={props.onClick}
      label={
        <Box display='flex' alignItems='center'>
          <Typography noWrap={true} sx={{ fontWeight: "inherit" }}>
            {props.labelText}
          </Typography>
          <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>

            {noConfig && <Tooltip title={intl.formatMessage({ id: 'services.none.tooltip' })}><AccountTreeOutlinedIcon sx={{ mx: 0.5, color: theme.palette.primary.dark, fontSize: 'medium' }} /></Tooltip>}
            {props.devMode && <Tooltip title={intl.formatMessage({ id: 'services.devmode.tooltip' })}><ConstructionIcon sx={iconStyle} /></Tooltip>}
            {props.disabledMode && <Tooltip title={intl.formatMessage({ id: 'services.disabledmode.tooltip' })}><NotInterestedIcon sx={iconStyle} /></Tooltip>}
            {props.anonMode && <Tooltip title={intl.formatMessage({ id: 'services.anonmode.tooltip' })}><FaceIcon sx={iconStyle} /></Tooltip>}

          </Box>
        </Box>
      }
    />
  );
}

interface LinkItemProps {
  labelText: string;
  nodeId: string;
  children?: React.ReactChild;
  onClick: () => void;
  devMode?: boolean;
}

const LinkItem: React.FC<LinkItemProps> = (props) => {
  const theme = useTheme();
  const intl = useIntl();

  const iconStyle = { mx: 0.5, color: theme.palette.primary.dark, fontSize: 'medium' };

  return (
    <TreeItemRoot
      itemId={props.nodeId}
      onClick={props.onClick}
      label={
        <Box display="flex" alignItems="center">
          <Typography noWrap sx={{ fontWeight: "inherit" }}>
            {props.labelText}
          </Typography>
          <Box display="flex" alignItems="center">
            {props.devMode && (
              <Tooltip title={intl.formatMessage({ id: 'services.devmode.tooltip' })}>
                <ConstructionIcon sx={iconStyle} />
              </Tooltip>
            )}
          </Box>
        </Box>
      }
    />
  );
};

interface ArticleItemOptions {
  setEditWorkflow: (workflowId: StencilApi.WorkflowId) => void,
  setEditLink: (linkId: StencilApi.LinkId) => void
}

const ArticleItem: React.FC<{
  articleId: StencilApi.ArticleId,
  nodeId?: string,
  options?: ArticleItemOptions
}> = ({ articleId, nodeId, options }) => {
  const theme = useTheme();
  const intl = useIntl();

  const { session, isArticleSaved } = Composer.useComposer();
  const view = session.getArticleView(articleId);
  const { article, pages, workflows, links } = view;
  const saved = isArticleSaved(article);
  const iconStyle = { mx: 0.5, color: theme.palette.primary.dark, fontSize: 'medium' };
  const articleName = session.getArticleName(view.article.id);


  return (
    <>
      <TreeItem itemId={nodeId ? nodeId : article.id}
        labelIcon={MenuBookOutlinedIcon}
        labelText={
          <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
            {articleName.name}
            {article.body.devMode && (
              <Tooltip title={intl.formatMessage({ id: 'services.devmode.tooltip' })}>
                <ConstructionIcon sx={iconStyle} />
              </Tooltip>
            )}
          </Box>
        }
      //labelcolor={saved ? "explorerItem" : "secondary.light"}
      >

        {/** Article options */}
        <EveliPermissions id='EDIT_STENCIL_ASSET'>
          {
            options ? (<TreeItem itemId={article.id + 'article-options-nested'} labelText={<FormattedMessage id="options" />}>
              <ArticleOptions article={article} />
            </TreeItem>) : null
          }
        </EveliPermissions>

        {/** Pages */}
        <TreeItem itemId={article.id + 'pages-nested'}
          labelText={<FormattedMessage id="pages" />}
          labelInfo={`${pages.length}`}
          labelcolor={saved ? "page" : "secondary.light"}>
          {pages.map(pageView => (<ArticlePageItem key={pageView.page.id}
            article={view}
            page={pageView} />))}
        </TreeItem>


        {/** Workflows options */
          options ? (<TreeItem itemId={article.id + 'workflows-nested'}
            labelText={<FormattedMessage id="services" />}
            labelInfo={`${workflows.length}`}
            labelcolor={theme.palette.primary.dark}>

            {workflows
              .map((w) => ({ w, name: session.getWorkflowName(w.workflow.id)?.name }))
              .sort((a, b) => a.name.localeCompare(b.name))
              .map((w) => w.w)
              .map(view => (<WorkflowItem
                key={view.workflow.id}
                labelText={session.getWorkflowName(view.workflow.id).name}
                devMode={view.workflow.body.devMode}
                disabledMode={view.workflow.body.disabled}
                anonMode={view.workflow.body.anon}
                nodeId={view.workflow.id}

                onClick={() => options.setEditWorkflow(view.workflow.id)} />))}
          </TreeItem>) : null
        }

        {/** Links options */
          options ? (<TreeItem itemId={article.id + 'links-nested'}
            labelText={<FormattedMessage id="links" />}
            labelInfo={`${links.length}`}
            labelcolor={theme.palette.primary.light}>

            {links
              .map((w) => ({ w, name: session.getLinkName(w.link.id)?.name }))
              .sort((a, b) => a.name.localeCompare(b.name))
              .map((w) => w.w)
              .map(view => (<LinkItem key={view.link.id}
                labelText={session.getLinkName(view.link.id).name}
                nodeId={view.link.id}
                onClick={() => options.setEditLink(view.link.id)}
                devMode={view.link.body.devMode} />)
              )}
          </TreeItem>) : null

        }
      </TreeItem>
    </>)
}

export type { ArticleItemOptions }
export default ArticleItem;
