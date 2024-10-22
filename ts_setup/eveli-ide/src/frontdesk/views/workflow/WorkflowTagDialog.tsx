import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Paper, Stack, TextField } from "@mui/material";
import { WorkflowRelease } from "../../types/WorkflowRelease";
import { WorkflowTable } from "./WorkflowTable";
import { FormattedMessage, useIntl } from "react-intl";

interface WorkflowTagDialogProps {
  workflowRelease : WorkflowRelease
  open: boolean
  setOpen: (open:boolean)=>void
}

export const WorkflowTagDialog: React.FC<WorkflowTagDialogProps> = ({workflowRelease, open, setOpen}) => {
  const intl = useIntl();
  return (
    <>
      <Dialog open={open} aria-labelledby='workflow-tag-dialog-title' maxWidth='md' fullWidth>
        <DialogTitle id='new-form-dialog-title'><FormattedMessage id='workflowRelease.dialogTitle' /></DialogTitle>
        <DialogContent>
          <Paper sx={{padding:'5px'}}>
            <Stack spacing={1}>
            <TextField
              id="workflow-tag-name"
              label={intl.formatMessage({id:'workflowReleaseTableHeader.name'})}
              defaultValue={workflowRelease.name}
              size='small'
              InputProps={{
                readOnly: true,
              }}
            />
            <TextField
              id="workflow-tag-description"
              label={intl.formatMessage({id:'workflowReleaseTableHeader.description'})}
              defaultValue={workflowRelease.description}
              size='small'
              InputProps={{
                readOnly: true,
              }}
            />
            </Stack>
          </Paper>
          <WorkflowTable workflows={workflowRelease.entries} refreshWorkflows={()=>{}} historyView={true}/>
        </DialogContent>
        <DialogActions>
          <Button onClick={()=>setOpen(false)} variant='contained'><FormattedMessage id='button.close' /></Button>
        </DialogActions>
      </Dialog>
    </>
  );
}