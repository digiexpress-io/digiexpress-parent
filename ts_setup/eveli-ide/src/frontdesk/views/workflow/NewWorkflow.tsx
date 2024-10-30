import { Dialog, DialogActions, DialogContent, DialogTitle, Grid, MenuItem } from '@mui/material';
import { Field, Form, Formik } from 'formik';
import { TextField } from 'formik-mui';
import React, { useContext, useMemo } from 'react';
import { FormattedMessage, useIntl, defineMessages } from 'react-intl';
import { useConfig } from '../../context/ConfigContext';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { useFetch } from '../../hooks/useFetch';
import { Workflow } from '../../types/Workflow';
import { DialobFormTag } from '../../types';
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

export interface NewFormFormProps {
  onSubmit: () => void;
  workflow: Workflow|null;
  open: boolean;
  setOpen: (open:boolean)=>void;
  dialobTags: DialobFormTag[];
}

export const NewWorkflow: React.FC<NewFormFormProps> = ({onSubmit, workflow, open, setOpen, dialobTags}) => {
  const intl = useIntl();
  
  const apiUrl = useConfig().wrenchApiUrl;
  const { response:flows } = useFetch<string[]>(`${apiUrl}/workflowAssets/`);

  const session = useContext(SessionRefreshContext);
  const { enqueueSnackbar } = useSnackbar();

  const handleClose = () => {
    setOpen(false);
  }

  const handleSubmit = (workflowCommand: Workflow):void => {
    let method = 'POST';
    let url = `${apiUrl}/workflows/`;

    if (workflowCommand.id) {
      method = 'PUT';
      url = url + workflowCommand.id;
    }

    session.cFetch(`${url}`,{
      method: method,
      headers: {
        'Accept': 'application/json'
      },
      body: workflowCommand
    })
    .then((response:any) => {
      if (response.ok) {
        setOpen(false);
        onSubmit();
      }
      else {
        enqueueSnackbar(intl.formatMessage({id: 'error.workflowCreation'}), {variant: 'error'});
      }
    })
  }
  
  const forms = useMemo(()=>{
    const result:Map<string, string> = new Map();
    dialobTags?.forEach(tag=> {
      result.set(tag.formName, tag.formLabel);
    })
    return Array.from(result);
  }, [dialobTags]);

  const requiredValidator = (value:any) => !value ? intl.formatMessage(messages.requiredError) : undefined;
  
  return (
    <>
    
      <Dialog open={open} onClose={handleClose} aria-labelledby='new-form-dialog-title' maxWidth='md' fullWidth>
        <DialogTitle id='new-form-dialog-title'><FormattedMessage id='workflow.dialogTitle' /></DialogTitle>

          <Formik
            initialValues={workflow || {
              name: '',
              flowName: '',
              formName:'',
              formTag: ''
            }}
            enableReinitialize={true}
            onSubmit={(values, {setSubmitting}) => {
              handleSubmit(values as Workflow); 
              setSubmitting(false);
            }}
          >
            {
              ({values, submitForm, isSubmitting, errors, isValid}) => (
                <Form>
                  <DialogContent>
                    <Grid container spacing={1} >
                      <Grid item xs={12} md={12}>
                        <Field component={TextField} name='name' label={intl.formatMessage({id: 'workflow.name'})} 
                          fullWidth required  validate={requiredValidator} error={!!errors.name}
                          helperText={errors.name} />
                      </Grid>
                      <Grid item xs={12} md={6}>
                        <Field component={TextField} name='formName' select
                          label={intl.formatMessage({id: 'workflow.form.formName'})} 
                          fullWidth required  validate={requiredValidator} error={!!errors.formName}
                          helperText={errors.formName} >
                          {
                            forms.map((namelabel, index) => <MenuItem key={index} value={namelabel[0]}>{namelabel[1]}</MenuItem>)
                          }
                        </Field>
                      </Grid>
                      <Grid item xs={12} md={6}>
                        <Field component={TextField} name='formTag' select
                          label={intl.formatMessage({id: 'workflow.form.formTag'})} 
                          fullWidth required  validate={requiredValidator} error={!!errors.formTag}
                          helperText={errors.formTag} >
                          {
                            dialobTags?.filter(tag=>tag.formName === values.formName)
                            .map((tag, i) => <MenuItem key={i} value={tag.tagName}>{tag.tagName}</MenuItem>  )
                          }
                        </Field>
                      </Grid>
                      <Grid item xs={12} md={12}>
                        <Field component={TextField} name='flowName' select
                          label={intl.formatMessage({id: 'workflow.flowName'})} 
                          fullWidth required  validate={requiredValidator} error={!!errors.flowName}
                          helperText={errors.flowName} >
                          {
                            flows?.map((name, index) => <MenuItem key={index} value={name}>{name}</MenuItem>)
                          }
                        </Field>
                      </Grid>
                    </Grid>
                  </DialogContent>
                  <DialogActions>
                    <Burger.SecondaryButton onClick={handleClose} label={intl.formatMessage({ id: 'button.cancel' })} />
                    <Burger.PrimaryButton onClick={submitForm} disabled={isSubmitting || !isValid} label={intl.formatMessage({ id: 'button.accept' })} />
                  </DialogActions>
                  </Form>
                )
            }
          </Formik>
       
      </Dialog>
    </>
  );
}