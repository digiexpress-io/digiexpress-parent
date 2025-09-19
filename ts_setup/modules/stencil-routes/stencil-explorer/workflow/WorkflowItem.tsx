import * as React from "react";
import { Box, Tooltip, useTheme } from '@mui/material';

import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';
import ConstructionIcon from '@mui/icons-material/Construction';
import NotInterestedIcon from '@mui/icons-material/NotInterested';
import AssignmentIndIcon from '@mui/icons-material/AssignmentInd';
import FaceIcon from '@mui/icons-material/Face';

import { FormattedMessage, useIntl } from 'react-intl';


import { EveliPermissions, TreeItem } from "@dxs-ts/eveli-primitives";
import { StencilApi, StencilComposerApi as Composer } from '@dxs-ts/stencil-api';

import { WorkflowOptions } from './WorkflowOptions';
import ArticleItem from '../article/ArticleItem';


const WorkflowItem: React.FC<{ workflowId: StencilApi.WorkflowId }> = ({ workflowId }) => {
  const intl = useIntl();
  const theme = useTheme();

  const { session } = Composer.useComposer();
  const view = session.getWorkflowView(workflowId);
  const { workflow } = view;

  const workflowName = session.getWorkflowName(workflow.id);
  const iconStyle = { mx: 0.5, color: theme.palette.primary.dark, fontSize: 'medium' };

  return (
    <>
      <TreeItem
        itemId={workflow.id}
        labelText={<Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          {workflowName.name}
          {workflow.body.devMode && <Tooltip title={intl.formatMessage({ id: 'services.devmode.tooltip' })}><ConstructionIcon sx={iconStyle} /></Tooltip>}
          {workflow.body.disabled && <Tooltip title={intl.formatMessage({ id: 'services.disabledmode.tooltip' })}><NotInterestedIcon sx={iconStyle} /></Tooltip>}
          {workflow.body.anon && <Tooltip title={intl.formatMessage({ id: 'services.anonmode.tooltip' })}><FaceIcon sx={iconStyle} /></Tooltip>}
          {workflow.body.assignable && <Tooltip title={intl.formatMessage({ id: 'services.assignable.tooltip' })}><AssignmentIndIcon sx={iconStyle} /></Tooltip>}
        </Box>
        }
        labelcolor="explorerItem"
        labelIcon={AccountTreeOutlinedIcon}>

        <EveliPermissions id='EDIT_STENCIL_ASSET'>
          <TreeItem itemId={workflow.id + 'options-nested'} labelText={<FormattedMessage id="options" />} labelIcon={EditIcon}>
            <WorkflowOptions workflow={workflow} />
          </TreeItem>
        </EveliPermissions>

        {/** Article options */}
        <TreeItem itemId={workflow.id + 'articles-nested'}
          labelText={<FormattedMessage id="articles" />}
          labelIcon={FolderOutlinedIcon}
          labelInfo={`${workflow.body.articles.length}`}
          labelcolor="primary">

          {workflow.body.articles.map((id => session.getArticleView(id))).map(view => (
            <ArticleItem key={view.article.id} articleId={view.article.id} nodeId={`${workflow.id}-${view.article.id}-nested`} />
          ))}
        </TreeItem>

      </TreeItem>
    </>)
}

export default WorkflowItem;
