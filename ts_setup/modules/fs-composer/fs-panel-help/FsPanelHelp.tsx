import React from 'react';
import { Typography, Box } from '@mui/material';
import { OpenInNew as OpenInNewIcon } from '@mui/icons-material';
import ReactMarkdown from 'react-markdown';
import { Fs } from '@dxs-ts/fs-api';

import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelHelpProps } from './FsPanelHelpProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsPanelHelpRoot } from './useUtilityClasses';
import { helpFolder } from './help-folder';
import { helpArticle } from './help-article';
import { helpArticlePage } from './help-article-page';
import { helpArticleTemplate } from './help-article-template';
import { helpArticleWorkflow } from './help-article-workflow';
import { helpArticleLink } from './help-article-link';
import { helpFlow } from './help-flow';
import { helpFlowTask } from './help-flow-task';
import { helpDecisionTable } from './help-decision-table';
import { helpDialobForm } from './help-dialob-form';
import { helpPrintout } from './help-printout';
import { helpPrintoutPage } from './help-printout-page';
import { helpPrintoutResource } from './help-printout-resource';
import { helpLocale } from './help-locale';
import { helpDeployment } from './help-deployment';

const HELP_CONTENT: Partial<Record<Fs.BodyType, string>> = {
  FOLDER: helpFolder,
  ARTICLE: helpArticle,
  ARTICLE_PAGE: helpArticlePage,
  ARTICLE_TEMPLATE: helpArticleTemplate,
  ARTICLE_WORKFLOW: helpArticleWorkflow,
  ARTICLE_LINK: helpArticleLink,
  FLOW: helpFlow,
  FLOW_TASK: helpFlowTask,
  DECISION_TABLE: helpDecisionTable,
  DIALOB_FORM: helpDialobForm,
  PRINTOUT: helpPrintout,
  PRINTOUT_PAGE: helpPrintoutPage,
  PRINTOUT_RESOURCE: helpPrintoutResource,
  LOCALE: helpLocale,
  DEPLOYMENT: helpDeployment,
};


export const FsPanelHelp: React.FC<FsPanelHelpProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  const linkRenderer = (props: any) => {
    return (
      <a href={props.href} target="_blank" rel="noreferrer" className={classes.externalLink}>
        {props.children}
        <OpenInNewIcon fontSize="small" />
      </a>
    );
  };

  return (
    <FsPanel title="Help" icon={<FsIcon icon={FsIcons.Help} large />} activeDirent={true}>
      <FsPanelHelpRoot className={classes.root} ownerState={ownerState}>
        <ReactMarkdown components={
          {
            h1: (props) => (<Typography className={classes.h1}>{props.children}</Typography>),
            h2: (props) => (<Typography className={classes.h2}>{props.children}</Typography>),
            h3: (props) => (<Typography className={classes.h3}>{props.children}</Typography>),
            h4: (props) => (<Typography className={classes.h4}>{props.children}</Typography>),
            h5: (props) => (<Typography className={classes.h5}>{props.children}</Typography>),
            h6: (props) => (<Typography className={classes.h6}>{props.children}</Typography>),
            p: (props) => (<Typography className={classes.paragraph}>{props.children}</Typography>),
            li: (props) => (<li className={classes.listItem}><Typography component="span" variant={'body1'}>{props.children}</Typography></li>),
            a: linkRenderer,
            code: (props) => (
              <Box component="code" className={classes.codeBlock}>
                <Box className={classes.codeContent}>
                  {props.children}
                </Box>
              </Box>
            ),
            ...(props.overrides ?? {})
          }}
        >
          {(props.dirent?.type ? HELP_CONTENT[props.dirent.type] : undefined) ?? helpMarkdownDefault}
        </ReactMarkdown>
      </FsPanelHelpRoot>
    </FsPanel>
  );
};


const helpMarkdownDefault = `
# TODO
`;
