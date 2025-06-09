import React from 'react';
import { Accordion, AccordionDetails, AccordionSummary, Badge, Typography, generateUtilityClass, styled, useThemeProps } from '@mui/material';
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

import { FormattedMessage } from 'react-intl';

import { TaskApi } from '@/api-task';
import { EveliTaskComments } from '@/eveli-task-comments';
import composeClasses from '@mui/utils/composeClasses';


export interface EveliTaskClientMessagesProps {
  task: TaskApi.Task,
  onReload: () => Promise<void>
}

export const EveliTaskClientMessages: React.FC<EveliTaskClientMessagesProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const ownerState = {
    ...props
  }

  const classes = useUtilityClasses();

  return (
    <EveliTaskClientMessagesRoot className={classes.root} ownerState={ownerState}>
      <Accordion>
        <AccordionSummary expandIcon={<ExpandMoreIcon />} className={classes.accordionSummary} >
          <Typography className={classes.accordionTitle}><FormattedMessage id="externalComments" /></Typography>
          <Badge badgeContent={ownerState.task.comments.filter(c => c.external).length} color="warning"><ChatBubbleOutlineIcon /></Badge>
        </AccordionSummary>
        <AccordionDetails className={classes.accordionDetails}>
          <EveliTaskComments task={ownerState.task} isExternalThread={true} reload={ownerState.onReload} />
        </AccordionDetails>
      </Accordion>
    </EveliTaskClientMessagesRoot>
  )
}


export const MUI_NAME = 'EveliTaskClientMessages';
export interface EveliTaskClientMessagesClasses {
  root: string;
  accordionTitle: string;
  accordionSummary: string;
  accordionDetails: string;
}

export type EveliTaskClientMessagesClassKey = keyof EveliTaskClientMessagesClasses;


export const EveliTaskClientMessagesRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Search',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.accordionTitle,
      styles.accordionSummary,
      styles.accordionDetails
    ];
  },
})<{ ownerState: EveliTaskClientMessagesProps }>(({ theme }) => {
  return {
    padding: theme.spacing(1),
    width: '100%',

  }
})

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    accordionTitle: ['accordionTitle'],
    accordionSummary: ['accordionSummary'],
    accordionDetails: ['accordionDetails']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

