import React from 'react';
import { Accordion, AccordionDetails, AccordionSummary, Box, Typography, generateUtilityClass, styled, useThemeProps } from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

import { FormattedMessage } from 'react-intl';

import { TaskApi } from '@/api-task';
import { EveliTaskComments } from '@/eveli-task-comments';
import composeClasses from '@mui/utils/composeClasses';
import { EveliTaskCountIndicator } from './EveliTaskCountIndicator';


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
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Box className={classes.spacer}>
          <Typography className={classes.accordionTitle}><FormattedMessage id="externalComments" /></Typography>
            <EveliTaskCountIndicator count={ownerState.task.comments.filter(c => c.external).length} />
          </Box>
        </AccordionSummary>
        <AccordionDetails>
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
  accordionDetails: string;
  spacer: string;
}

export type EveliTaskClientMessagesClassKey = keyof EveliTaskClientMessagesClasses;


export const EveliTaskClientMessagesRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.accordionTitle,
    ];
  },
})<{ ownerState: EveliTaskClientMessagesProps }>(({ theme }) => {
  return {
    width: '100%',
    '& .EveliTaskClientMessages-accordionTitle': {
      fontWeight: 'bold',
      marginRight: theme.spacing(2)
    },
    '& .EveliTaskClientMessages-spacer': {
      width: '17%',
      display: 'flex',
      justifyContent: 'space-between'
    }
  }
})

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    accordionTitle: ['accordionTitle'],
    spacer: ['spacer']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

