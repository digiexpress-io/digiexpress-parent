import React from 'react';
import { Box, Dialog, DialogActions, DialogContent, DialogTitle, Stack, Typography } from "@mui/material";
import { WorkflowRelease } from "../../types/WorkflowRelease";
import { WorkflowTable } from "./WorkflowTable";
import { FormattedMessage, useIntl } from "react-intl";

import * as Burger from '@/burger';


interface WorkflowTagDialogProps {
  workflowRelease: WorkflowRelease
  open: boolean
  setOpen: (open: boolean) => void
}

export const WorkflowTagDialog: React.FC<WorkflowTagDialogProps> = ({ workflowRelease, open, setOpen }) => {
  const intl = useIntl();

  return (
    <>
      <Dialog open={open} aria-labelledby='workflow-tag-dialog-title' maxWidth='md' fullWidth>
        <DialogTitle id='new-form-dialog-title'><FormattedMessage id='workflowRelease.dialogTitle' /></DialogTitle>
        <DialogContent>
          <Box sx={{ padding: 1, mb: 1 }}>
            <Stack spacing={1}>
              <Box display='flex' gap={1}>
                <Typography variant="body1" fontWeight='bold'>{intl.formatMessage({ id: 'workflowReleaseTableHeader.name' })}</Typography>
                <Typography>{workflowRelease.body.name}</Typography>
              </Box>

              <Box display='flex' gap={1}>
                <Typography variant="body1" fontWeight='bold'>{intl.formatMessage({ id: 'workflowReleaseTableHeader.description' })}</Typography>
                <Typography>{workflowRelease.body.description}</Typography>
              </Box>
            </Stack>
          </Box>
          <WorkflowTable workflows={workflowRelease.body.entries.map(body => ({ body, id: '', type: '' }))} refreshWorkflows={() => { }} historyView={true} />
        </DialogContent>
        <DialogActions>
          <Burger.SecondaryButton onClick={() => setOpen(false)} label='button.close' />
        </DialogActions>
      </Dialog>
    </>
  );
}