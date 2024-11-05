import { Dialog, DialogActions, DialogContent, DialogTitle, Grid2, MenuItem } from '@mui/material';
import { Field, Form, Formik } from 'formik';
import { TextField } from 'formik-mui';
import React, { useContext, useMemo } from 'react';
import { useIntl, defineMessages } from 'react-intl';
import { useConfig } from '../../context/ConfigContext';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { useFetch } from '../../hooks/useFetch';
import { Workflow } from '../../types/Workflow';
import { DialobFormTag } from '../../types';
import { useSnackbar } from 'notistack';
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

export interface CreateOrEditWorkflowDialogProps {
  onSubmit: () => void;
  workflow: Workflow | null;
  open: boolean;
  setOpen: (open: boolean) => void;
  dialobTags: DialobFormTag[];
}

export const CreateOrEditWorkflowDialog: React.FC<CreateOrEditWorkflowDialogProps> = ({ onSubmit, workflow, open, setOpen, dialobTags }) => {
  const intl = useIntl();

  const { serviceUrl } = useConfig();
  const { response: flows } = useFetch<string[]>(`${serviceUrl}rest/api/assets/wrench/flow-names`);

  const session = useContext(SessionRefreshContext);
  const { enqueueSnackbar } = useSnackbar();

  const handleClose = () => {
    setOpen(false);
  }

  const handleSubmit = (workflowCommand: Workflow): void => {
    let method = 'POST';
    let url = `${serviceUrl}rest/api/assets/workflows`;

    if (workflowCommand.id) {
      method = 'PUT';
      url = url + '/' + workflowCommand.id;
    }

    session.cFetch(`${url}`, {
      method: method,
      headers: {
        'Accept': 'application/json'
      },
      body: { ...workflowCommand.body, ...(workflowCommand.id ? { id: workflowCommand.id } : {}) }
    })
      .then((response: any) => {
        if (response.ok) {
          setOpen(false);
          onSubmit();
        }
        else {
          enqueueSnackbar(intl.formatMessage({ id: 'error.workflowCreation' }), { variant: 'error' });
        }
      })
  }

  const forms = useMemo(() => {
    const result: Map<string, string> = new Map();
    dialobTags?.forEach(tag => {
      result.set(tag.formName, tag.formLabel);
    })
    return Array.from(result);
  }, [dialobTags]);

  const requiredValidator = (value: any) => !value ? intl.formatMessage(messages.requiredError) : undefined;

  return (
    <>

      <Dialog open={open} onClose={handleClose} aria-labelledby='new-form-dialog-title' maxWidth='md' fullWidth>
        <DialogTitle id='new-form-dialog-title'><TableHeader id='workflow.dialogTitle' /></DialogTitle>
        <Formik
          initialValues={workflow || {
            body: {
              name: '',
              flowName: '',
              formName: '',
              formTag: ''
            }
          }}
          enableReinitialize={true}
          onSubmit={(values, { setSubmitting }) => {
            handleSubmit(values as Workflow);
            setSubmitting(false);
          }}
        >
          {
            ({ values, submitForm, isSubmitting, errors, isValid }) => (
              <Form>
                <DialogContent>
                  <Grid2 container spacing={1} >
                    <Grid2 size={{ xs: 12, md: 12 }}>
                      <Field component={TextField} name='body.name' label={intl.formatMessage({ id: 'workflow.name' })}
                        fullWidth required validate={requiredValidator} error={!!errors.body?.name}
                        helperText={errors.body?.name} />
                    </Grid2>
                    <Grid2 size={{ xs: 12, md: 6 }}>
                      <Field component={TextField} name='body.formName' select
                        label={intl.formatMessage({ id: 'workflow.form.formName' })}
                        fullWidth required validate={requiredValidator} error={!!errors.body?.formName}
                        helperText={errors.body?.formName} >
                        {
                          forms.map((namelabel, index) => <MenuItem key={index} value={namelabel[0]}>{namelabel[1]}</MenuItem>)
                        }
                      </Field>
                    </Grid2>
                    <Grid2 size={{ xs: 12, md: 6 }}>
                      <Field component={TextField} name='body.formTag' select
                        label={intl.formatMessage({ id: 'workflow.form.formTag' })}
                        fullWidth required validate={requiredValidator} error={!!errors.body?.formTag}
                        helperText={errors.body?.formTag} >
                        {
                          dialobTags?.filter(tag => tag.formName === values.body?.formName)
                            .map((tag, i) => <MenuItem key={i} value={tag.tagName}>{tag.tagName}</MenuItem>)
                        }
                      </Field>
                    </Grid2>
                    <Grid2 size={{ xs: 12, md: 12 }}>
                      <Field component={TextField} name='body.flowName' select
                        label={intl.formatMessage({ id: 'workflow.flowName' })}
                        fullWidth required validate={requiredValidator} error={!!errors.body?.flowName}
                        helperText={errors.body?.flowName}>
                        {
                          flows?.map((name, index) => <MenuItem key={index} value={name}>{name}</MenuItem>)
                        }
                      </Field>
                    </Grid2>
                  </Grid2>
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