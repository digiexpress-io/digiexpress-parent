import React, { useMemo } from 'react';
import { Dialog, DialogActions, DialogContent, DialogTitle, Grid2, MenuItem, Button } from '@mui/material';
import { Field, Form, Formik } from 'formik';
import { TextField } from 'formik-mui';
import { useIntl, defineMessages, FormattedMessage } from 'react-intl';
import { useFetch } from '@dxs-ts/eveli-fetch';

import { Workflow } from '../../types/Workflow';
import { DialobFormTag } from '../../types';


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
  const { update: handleSubmit } = useFetch('worker/rest/api/assets/workflows/$workflowId.PUT', {});
  const { flows } = useFetch('worker/rest/api/assets/wrench/flow-names.GET', {});

  const handleClose = () => {
    setOpen(false);
  }

  const forms = useMemo(() => {
    const result: Map<string, string> = new Map();
    dialobTags?.forEach(tag => {
      result.set(tag.formName, tag.formLabel);
    })
    return Array.from(result).sort((a,b)=>a[1].localeCompare(b[1]));
  }, [dialobTags]);

  const requiredValidator = (value: any) => !value ? intl.formatMessage(messages.requiredError) : undefined;

  return (
    <>

      <Dialog open={open} onClose={handleClose} aria-labelledby='new-form-dialog-title' maxWidth='md' fullWidth>
        <DialogTitle fontWeight='bold' id='new-form-dialog-title'>{intl.formatMessage({ id: 'workflow.dialogTitle' })}</DialogTitle>
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
            handleSubmit(values as Workflow, () => {
              setOpen(false);
              onSubmit();
            });
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
                  <Button onClick={handleClose}  variant='text'><FormattedMessage id='button.cancel'/></Button>
                  <Button variant='contained' onClick={submitForm} disabled={isSubmitting || !isValid}  ><FormattedMessage id='button.accept'/></Button>
                </DialogActions>
              </Form>
            )
          }
        </Formik>

      </Dialog>
    </>
  );
}