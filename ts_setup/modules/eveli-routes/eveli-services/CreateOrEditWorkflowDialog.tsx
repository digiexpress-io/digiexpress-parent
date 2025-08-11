import React from 'react';
import { Dialog, DialogActions, DialogContent, DialogTitle, Grid2, MenuItem, Button, TextField } from '@mui/material';
import { useIntl, FormattedMessage } from 'react-intl';
import { PublicationApi } from '@dxs-ts/eveli-api';
import { useFetch } from '@dxs-ts/envir-fetch';
import { CancelButton } from '@dxs-ts/eveli-primitives';



export interface CreateOrEditWorkflowDialogProps {
  onSubmit: () => void;
  workflow: PublicationApi.AssetService;
  open: boolean;
  setOpen: (open: boolean) => void;
  dialobTags: PublicationApi.AssetFormTag[];
}

export const CreateOrEditWorkflowDialog: React.FC<CreateOrEditWorkflowDialogProps> = ({ onSubmit, workflow, open, setOpen, dialobTags }) => {
  const intl = useIntl();
  const { update } = useFetch('worker/rest/api/assets/workflows/$workflowId.PUT', {});
  const { flows } = useFetch('worker/rest/api/assets/wrench/flow-names.GET', {});
  const [form, setForm] = React.useState<PublicationApi.AssetService>(workflow);
  const [isSubmitting, setSubmitting] = React.useState<boolean>(false);

  const handleClose = () => {
    setOpen(false);
  }

  const forms = React.useMemo(() => {
    const result: Map<string, string> = new Map();
    dialobTags?.forEach(tag => {
      result.set(tag.formName, tag.formLabel);
    })
    return Array.from(result).sort((a,b)=>a[1].localeCompare(b[1]));
  }, [dialobTags]);

  function handleSubmit() {
    setSubmitting(true);
    update(form, () => {
      setOpen(false);
      onSubmit();
  
      setSubmitting(false);
    });
  }


  const isValid = (
    !!workflow?.body.name &&
    !!workflow?.body.formName &&
    !!workflow?.body.formTag &&
    !!workflow?.body.flowName
  );



  return (
      <Dialog open={open} onClose={handleClose} aria-labelledby='new-form-dialog-title' maxWidth='md' fullWidth>
        <DialogTitle fontWeight='bold' id='new-form-dialog-title'>{intl.formatMessage({ id: 'workflow.dialogTitle' })}</DialogTitle>
          <DialogContent>
            <Grid2 container spacing={1} >
              <Grid2 size={{ xs: 12, md: 12 }}>
                <TextField name='body.name' 
                  label={intl.formatMessage({ id: 'workflow.name' })}
                  fullWidth required 
                  error={!form.body.name}
                  helperText={workflow.body.name}
                  value={form.body.name}
                  onChange={(event) => {
                    const newValue = event.target.value;

                    setForm(prev => {
                      const next = {...prev, body: {...prev?.body}}
                      console.log(event);
                      next.body.name = newValue;
                      return next;
                    })
                  }
                  }  
                />
              </Grid2>
              <Grid2 size={{ xs: 12, md: 6 }}>
                <TextField name='body.formName' select
                  label={intl.formatMessage({ id: 'workflow.form.formName' })}
                  fullWidth required 
                  error={!form.body.formName}
                  helperText={form.body.formName}
                  value={form.body.formName}
                  onChange={(event) => { 
                    const newValue = event.target.value;
                    setForm(prev => {
                      const next = {...prev, body: {...prev?.body}}
                      next.body.formName = newValue;
                      return next;
                    })
                  }}>
                  {
                    forms.map((namelabel, index) => <MenuItem key={index} value={namelabel[0]}>{namelabel[1]}</MenuItem>)
                  }
                </TextField>
              </Grid2>
              <Grid2 size={{ xs: 12, md: 6 }}>
                <TextField name='body.formTag' select
                  label={intl.formatMessage({ id: 'workflow.form.formTag' })}
                  fullWidth required 
                  error={!form.body.formTag}
                  helperText={form.body.formTag}
                  value={form.body.formTag}
                  onChange={(event) => { 
                    const newValue = event.target.value;
                    setForm(prev => {
                      const next = {...prev, body: {...prev?.body}}
                      next.body.formTag = newValue;
                      return next;
                    })
                  }}>
                  {
                    dialobTags?.filter(tag => tag.formName === form.body.formName)
                      .map((tag, i) => <MenuItem key={i} value={tag.tagName}>{tag.tagName}</MenuItem>)
                  }
                </TextField>
              </Grid2>
              <Grid2 size={{ xs: 12, md: 12 }}>
                <TextField name='body.flowName' select
                  label={intl.formatMessage({ id: 'workflow.flowName' })}
                  fullWidth required 
                  error={!form.body.flowName}
                  helperText={form.body.flowName}
                  value={form.body.flowName}
                  onChange={(event) => {
                    const newValue = event.target.value;
                    setForm(prev => {
                      const next = {...prev, body: {...prev?.body}}
                      next.body.flowName = newValue;
                      return next;
                    })
                  }}>
                  {
                    flows?.map((name, index) => <MenuItem key={index} value={name}>{name}</MenuItem>)
                  }
                </TextField>
              </Grid2>
            </Grid2>
          </DialogContent>
          <DialogActions>
            <CancelButton onClick={handleClose} />
            <Button variant='contained' onClick={handleSubmit} disabled={isSubmitting || !isValid}><FormattedMessage id='button.accept'/></Button>
          </DialogActions>
      </Dialog>
  );
}