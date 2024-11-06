import React, { useContext } from 'react';
import { Dialog, DialogActions, DialogContent, DialogTitle, Stack } from '@mui/material';
import { Field, Form, Formik } from 'formik';
import { TextField } from 'formik-mui';
import { useIntl, defineMessages } from 'react-intl';
import { useSnackbar } from 'notistack';

import { useConfig } from '../../context/ConfigContext';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { handleErrors } from '../../util/cFetch';
import { TableHeader } from '../../components/TableHeader';

import * as Burger from '@/burger';

const messages = defineMessages(
  {
    requiredError: {
      id: "error.valueRequired"
    },
    minLengthError: {
      id: "error.minTextLength"
    }
  }
);

export interface CreateWorkflowTagDialogProps {
  onSubmit: () => void;
  open: boolean;
  setOpen: (open: boolean) => void
}

export const CreateWorkflowTagDialog: React.FC<CreateWorkflowTagDialogProps> = ({ onSubmit, open, setOpen }) => {
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const { serviceUrl } = useConfig();

  const session = useContext(SessionRefreshContext);

  const handleClose = () => {
    setOpen(false);
  }

  const handleSubmit = (workflowReleaseCommand: { name: string, description: string }): void => {
    let method = 'POST';
    let url = `${serviceUrl}rest/api/assets/workflows/tags`;

    session.cFetch(`${url}`, {
      method: method,
      headers: {
        'Accept': 'application/json'
      },
      body: workflowReleaseCommand
    })
      .then(response => handleErrors(response))
      .then((response: any) => {
        setOpen(false);
        onSubmit();
      })
      .catch(error => {
        enqueueSnackbar(intl.formatMessage({ id: 'workflowRelease.releaseCreationFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
      });

  }

  const requiredValidator = (value: any) => !value ? intl.formatMessage(messages.requiredError) : undefined;

  return (
    <Dialog open={open} onClose={handleClose} aria-labelledby='new-form-dialog-title' maxWidth='md' fullWidth>
      <DialogTitle id='new-form-dialog-title'><TableHeader id='workflowRelease.dialogTitle' /></DialogTitle>

      <Formik
        initialValues={{
          name: '',
          description: ''
        }}
        enableReinitialize={true}
        onSubmit={(values, { setSubmitting }) => {
          handleSubmit(values as any);
          setSubmitting(false);
        }}
      >
        {
          ({ submitForm, isSubmitting, errors, isValid }) => (
            <Form>
              <DialogContent>
                <Stack spacing={1}>
                  <Field component={TextField} name='name' label={intl.formatMessage({ id: 'workflowRelease.name' })}
                    fullWidth required validate={requiredValidator} error={!!errors.name}
                    helperText={errors.name} InputProps={{ margin: 'normal' }} />
                  <Field component={TextField} name='description' label={intl.formatMessage({ id: 'workflowRelease.description' })}
                    fullWidth InputProps={{ margin: 'normal' }} />
                </Stack>
              </DialogContent>
              <DialogActions>
                <Burger.SecondaryButton onClick={handleClose} label='button.cancel' />
                <Burger.PrimaryButton onClick={submitForm} disabled={isSubmitting || !isValid} label='button.accept' />
              </DialogActions>
            </Form>
          )
        }
      </Formik>
    </Dialog>
  );
}