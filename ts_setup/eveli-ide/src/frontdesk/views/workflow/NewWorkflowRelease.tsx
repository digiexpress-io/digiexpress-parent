import { Dialog, DialogActions, DialogContent, DialogTitle, Stack } from '@mui/material';
import { Field, Form, Formik } from 'formik';
import { TextField } from 'formik-mui';
import React, { useContext } from 'react';
import { FormattedMessage, useIntl, defineMessages } from 'react-intl';
import { useConfig } from '../../context/ConfigContext';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { WorkflowRelease } from '../../types/WorkflowRelease';
import { handleErrors } from '../../util/cFetch';
import { useSnackbar } from 'notistack';

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

export interface NewFormProps {
  onSubmit: () => void;
  workflowRelease: WorkflowRelease | null;
  open: boolean;
  setOpen: (open: boolean) => void
}

export const NewWorkflowRelease: React.FC<NewFormProps> = ({ onSubmit, workflowRelease, open, setOpen }) => {
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const apiUrl = useConfig().wrenchApiUrl;

  const session = useContext(SessionRefreshContext);

  const handleClose = () => {
    setOpen(false);
  }

  const handleSubmit = (workflowReleaseCommand: WorkflowRelease): void => {
    let method = 'POST';
    let url = `${apiUrl}/workflowReleases/`;

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
    <>

      <Dialog open={open} onClose={handleClose} aria-labelledby='new-form-dialog-title' maxWidth='md' fullWidth>
        <DialogTitle id='new-form-dialog-title'><FormattedMessage id='workflowRelease.dialogTitle' /></DialogTitle>

        <Formik
          initialValues={{
            body: {
              name: workflowRelease?.body.name,
              description: workflowRelease?.body.description
            }
          }}
          enableReinitialize={true}
          onSubmit={(values, { setSubmitting }) => {
            handleSubmit(values as WorkflowRelease);
            setSubmitting(false);
          }}
        >
          {
            ({ submitForm, isSubmitting, errors, isValid }) => (
              <Form>
                <DialogContent>
                  <Stack spacing={1}>
                    <Field component={TextField} name='name' label={intl.formatMessage({ id: 'workflowRelease.name' })}
                      fullWidth required validate={requiredValidator} error={!!errors.body?.name}
                      helperText={errors.body?.name} InputProps={{ margin: 'normal' }} />
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
    </>
  );
}