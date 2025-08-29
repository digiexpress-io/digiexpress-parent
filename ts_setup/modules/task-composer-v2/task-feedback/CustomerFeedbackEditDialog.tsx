import React from 'react';

import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, styled, Zoom } from '@mui/material';
import { useIntl } from 'react-intl';
import composeClasses from '@mui/utils/composeClasses';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { UpsertOneFeedback } from '@dxs-ts/task-feedback';
import { PublishedNotifier } from './PublishedNotifier';



type DelegateButtonProps = { disabled: boolean, onClick: () => Promise<void> };

interface CallbackForSave {
  onClick: () => Promise<void> 
}

const DummyCallback: CallbackForSave = {
  onClick: async () => {}
}

export interface CustomerFeedbackEditProps {
  task: TaskApi.Task,
  open: boolean,
  onClose: () => void
}

export const CustomerFeedbackEditDialog: React.FC<CustomerFeedbackEditProps> = ({ open, onClose, task }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const backend = useTaskBackend();
  const [saveDisabled, setSaveDisabled] = React.useState(true);
  const [saveDelegate, setSaveDelegate] = React.useState<CallbackForSave>(DummyCallback);
  
  function onFeedbackCancel() {
    backend.navigate.openOneTask(task.taskRef!)
  }

  const AcceptButton: React.ElementType<DelegateButtonProps> = React.useCallback(
    (props: DelegateButtonProps) => {

      React.useEffect(() => {
        setSaveDisabled(props.disabled);
      }, [props.disabled]);

      React.useEffect(() => {
        setSaveDelegate({ onClick: props.onClick });
      }, [props.onClick]);
      return (<></>);
    }, []);

  const CancelButton: React.ElementType<DelegateButtonProps> = React.useCallback(
    (props: DelegateButtonProps) => {
      return (<></>);
    }, []);

  async function handleSave() {
    await saveDelegate.onClick();
    onClose();
  }

  

  return (
    <StyledDialog fullScreen className={classes.customerFeedbackEdit} open={open} onClose={onClose} slots={{ transition: Zoom }}>

      <DialogTitle sx={{ display: 'flex' }}>
        {intl.formatMessage({ id: 'task.customerFeedback', defaultMessage: 'Customer feedback' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        {task.taskRef ?? 'no task reference id'}
        <Box flexGrow={1} />
        <div>
          <PublishedNotifier task={task} />
        </div>
      </DialogTitle>

      <DialogContent>
        <UpsertOneFeedback 
          taskRef={task.taskRef!} 
          onComplete={() => { }} 
          onCancel={onFeedbackCancel}
          onDelete={onFeedbackCancel}
          reload={0} 
          allowDelete={false} 
          slots={{ AcceptButton, CancelButton }} />
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button disabled={saveDisabled} onClick={handleSave}>{intl.formatMessage({ id: 'button.save' })}</Button>
      </DialogActions>
    </StyledDialog>
  )
}





const MUI_NAME = 'CustomerFeedbackEditDialog';
const StyledDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.customerFeedbackEdit
    ];
  },

})(({ theme }) => {

  return {
    height: '100vh',
    '.MuiDialogContent-root': {
      display: 'flex',
      flexDirection: 'column',
      // overflow: 'hidden'
    }
  };
})


const useUtilityClasses = () => {
  const slots = {
    customerFeedbackEdit: ['customerFeedbackEdit'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
