import { Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Stack } from '@mui/material';
import { Field, Form, Formik } from 'formik';
import { TextField } from 'formik-mui';
import React, { useContext } from 'react';
import { FormattedMessage, useIntl, defineMessages } from 'react-intl';
import { useConfig } from '../../context/ConfigContext';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { AssetRelease, AssetReleaseInit } from '../../types/AssetRelease';
import { useFetch } from '../../hooks/useFetch';
import { AssetTag } from '../../types/AssetTag';
import { useSnackbar } from 'notistack';
import { handleErrors } from '../../util/cFetch';

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

const NEW_TAG_VALUE = '-1';

export interface NewReleaseProps {
  onSubmit: () => void;
  open: boolean;
  setOpen: (open: boolean) => void
}

export const NewPublicationDialog: React.FC<NewReleaseProps> = ({ onSubmit, open, setOpen }) => {
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const { serviceUrl } = useConfig()

  const session = useContext(SessionRefreshContext);
  const { response: workflowTags } = useFetch<AssetTag[]>(`${serviceUrl}rest/api/assets/any-tags/workflow-tags`);
  const { response: wrenchTags } = useFetch<AssetTag[]>(`${serviceUrl}rest/api/assets/any-tags/wrench-tags`);
  const { response: contentTags } = useFetch<AssetTag[]>(`${serviceUrl}rest/api/assets/any-tags/stencil-tags`);


  const handleClose = () => {
    setOpen(false);
  }

  const handleSubmit = (assetReleaseCommand: AssetReleaseInit): void => {
    let method = 'POST';
    let url = `${serviceUrl}rest/api/assets/publications`;

    let init: AssetReleaseInit = { ...assetReleaseCommand }
    // clear markers for new release creation
    if (assetReleaseCommand.body.contentTag === NEW_TAG_VALUE) {
      init.body.contentTag = null;
    }
    if (assetReleaseCommand.body.wrenchTag === NEW_TAG_VALUE) {
      init.body.wrenchTag = null;
    }
    if (assetReleaseCommand.body.workflowTag === NEW_TAG_VALUE) {
      init.body.workflowTag = null;
    }

    session.cFetch(`${url}`, {
      method: method,
      headers: {
        'Accept': 'application/json'
      },
      body: init
    })
      .then(response => handleErrors(response))
      .then((response: any) => {
        setOpen(false);
        onSubmit();
      })
      .catch(error => {
        enqueueSnackbar(intl.formatMessage({ id: 'publications.tagCreationFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
      });
  }

  const requiredValidator = (value: any) => !value ? intl.formatMessage(messages.requiredError) : undefined;

  const TagComponent: React.FC<{ name: string, labelId: string, tags?: AssetTag[], newTag: string }> =
    ({ name, labelId, tags, newTag }) => (
      <Field component={TextField} select name={name} label={intl.formatMessage({ id: labelId })}
        fullWidth InputProps={{ margin: 'normal' }}>
        <MenuItem key='-1' value={NEW_TAG_VALUE}>{intl.formatMessage({ id: 'publications.createNewTag' }, { tag: newTag })}</MenuItem>
        {
          tags?.map(tag => <MenuItem key={tag.name} value={tag.name}>{tag.name} / {tag.description}</MenuItem>)
        }
      </Field>
    )

  return (
    <>

      <Dialog open={open} onClose={handleClose} aria-labelledby='new-form-dialog-title' maxWidth='md' fullWidth>
        <DialogTitle id='new-form-dialog-title'><FormattedMessage id='publications.dialogTitle' /></DialogTitle>

        <Formik
          initialValues={{
            id: 0,
            body: {
              name: '',
              description: '',
              workflowTag: NEW_TAG_VALUE,
              wrenchTag: NEW_TAG_VALUE,
              contentTag: NEW_TAG_VALUE
            }

          }}
          enableReinitialize={true}
          onSubmit={(values, { setSubmitting }) => {
            handleSubmit(values as AssetRelease);
            setSubmitting(false);
          }}
        >
          {
            ({ submitForm, isSubmitting, values, errors, isValid }) => (
              <Form>
                <DialogContent>
                  <Stack spacing={1}>
                    <Field component={TextField} name='name' label={intl.formatMessage({ id: 'publications.name' })}
                      fullWidth required validate={requiredValidator} error={!!errors.body?.name}
                      helperText={errors.body?.name} InputProps={{ margin: 'normal' }} />
                    <Field component={TextField} name='description' label={intl.formatMessage({ id: 'publications.description' })}
                      fullWidth InputProps={{ margin: 'normal' }} />
                    <TagComponent name='contentTag' labelId='publications.contentTag' newTag={values.body.name} tags={contentTags} />
                    <TagComponent name='workflowTag' labelId='publications.workflowTag' newTag={values.body.name} tags={workflowTags} />
                    <TagComponent name='wrenchTag' labelId='publications.wrenchTag' newTag={values.body.name} tags={wrenchTags} />
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