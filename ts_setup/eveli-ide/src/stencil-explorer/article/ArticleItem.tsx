import * as React from "react";
import { Box, Tooltip, Typography, useTheme } from "@mui/material";
import LinkIcon from '@mui/icons-material/Link';

import NotInterestedIcon from '@mui/icons-material/NotInterested';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import MenuBookOutlinedIcon from '@mui/icons-material/MenuBookOutlined';
import FaceIcon from '@mui/icons-material/Face';

import ConstructionIcon from '@mui/icons-material/Construction';
import { FormattedMessage, useIntl } from 'react-intl';

import * as Burger from '@/eveli-styles';
import { StencilComposerApi as Composer } from '../../stencil-setup';
import { StencilApi } from '@/api-stencil';
import { ArticleOptions } from './ArticleOptions';
import ArticlePageItem from './ArticlePageItem';
import { EveliPermissions } from "@/eveli-permissions";


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
    <Burger.TreeItemRoot
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

  return (
    <Burger.TreeItemRoot
      itemId={props.nodeId}
      onClick={props.onClick}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          <Box component={props.devMode ? ConstructionIcon : LinkIcon} color={theme.palette.primary.light} sx={{ pl: 1, mr: 1 }} />
          <Typography align="left" maxWidth="300px" noWrap={true} variant="body2"
            sx={{ fontWeight: "inherit", flexGrow: 1 }}
          >
            {props.labelText}
          </Typography>
        </Box>
      }
    />
  );
}


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

  const { session, isArticleSaved } = Composer.useComposer();
  const view = session.getArticleView(articleId);
  const { article, pages, workflows, links } = view;
  const saved = isArticleSaved(article);

  const articleName = session.getArticleName(view.article.id);
  return (
    <>
      <Burger.TreeItem itemId={nodeId ? nodeId : article.id}
        labelText={articleName.name}
        labelIcon={article.body.devMode ? ConstructionIcon : MenuBookOutlinedIcon}
      //labelcolor={saved ? "explorerItem" : "secondary.light"}
      >

        {/** Article options */}
        <EveliPermissions id='EDIT_STENCIL_ASSET'>
          {
            options ? (<Burger.TreeItem itemId={article.id + 'article-options-nested'} labelText={<FormattedMessage id="options" />}>
              <ArticleOptions article={article} />
            </Burger.TreeItem>) : null
          }
        </EveliPermissions>

        {/** Pages */}
        <Burger.TreeItem itemId={article.id + 'pages-nested'}
          labelText={<FormattedMessage id="pages" />}
          labelInfo={`${pages.length}`}
          labelcolor={saved ? "page" : "secondary.light"}>
          {pages.map(pageView => (<ArticlePageItem key={pageView.page.id}
            article={view}
            page={pageView} />))}
        </Burger.TreeItem>


        {/** Workflows options */
          options ? (<Burger.TreeItem itemId={article.id + 'workflows-nested'}
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
          </Burger.TreeItem>) : null
        }

        {/** Links options */
          options ? (<Burger.TreeItem itemId={article.id + 'links-nested'}
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
          </Burger.TreeItem>) : null

        }

      </Burger.TreeItem>
    </>)
}

export type { ArticleItemOptions }
export default ArticleItem;
