import * as React from "react";
import { Box, Tooltip, Typography, useTheme } from "@mui/material";

import { NotInterested as NotInterestedIcon } from '@mui/icons-material';
import { AccountTreeOutlined as AccountTreeOutlinedIcon } from '@mui/icons-material';
import { MenuBookOutlined as MenuBookOutlinedIcon } from '@mui/icons-material';
import { Face as FaceIcon } from '@mui/icons-material';
import { Construction as ConstructionIcon } from '@mui/icons-material';

import { FormattedMessage, useIntl } from 'react-intl';


import { TreeItem, TreeItemRoot } from "@dxs-ts/eveli-primitives";

import { TagomiApi, TagomiComposerApi as Composer } from "@dxs-ts/tagomi-api";
import { ServiceOptions } from "./ServiceOptions";
import { TemplateEditMenuItem } from "../tagomi-template";

//import { ArticleOptions } from './ArticleOptions';
//import ArticlePageItem from './ArticlePageItem';


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



const ServiceItem: React.FC<{ serviceId: TagomiApi.ServiceId, nodeId?: string }> = ({ serviceId, nodeId }) => {

  const theme = useTheme();
  const { session, isServiceSaved } = Composer.useComposer();
  const view = session.getServiceView(serviceId);
  const { service, labels, templates } = view;
  const iconStyle = { mx: 0.5, color: theme.palette.primary.dark, fontSize: 'medium' };

  const saved = isServiceSaved(service);


  return (
    <>
      <TreeItem itemId={nodeId ? nodeId : service.id}
        labelIcon={MenuBookOutlinedIcon}
        labelText={
          <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
            {service.serviceName}
          </Box>
        }
      //labelcolor={saved ? "explorerItem" : "secondary.light"}
      >

        {/** Service options */}
        <TreeItem itemId={service.id + 'service-options-nested'} labelText={<FormattedMessage id="options" />}>
          <ServiceOptions service={service} />
        </TreeItem>

        {/** Templates */}
        <TreeItem itemId={service.id + 'pages-nested'}
          labelText={<FormattedMessage id="templates" />}
          labelInfo={`${templates.length}`}
          labelcolor={saved ? "page" : "secondary.light"}>
          {templates.map(template => (<TemplateEditMenuItem service={view} template={template} />))}
        </TreeItem>
 


        {/** Workflows options 
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
       */ }

      </TreeItem>
    </>)
}

export default ServiceItem;
