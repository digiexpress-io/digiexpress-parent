import React, { useContext } from 'react';

import { Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Stack, Button } from '@mui/material';

import { Field, Form, Formik } from 'formik';
import { TextField } from 'formik-mui';
import { useSnackbar } from 'notistack';
import { useIntl, defineMessages, FormattedMessage } from 'react-intl';

import { useConfig } from '../../context/ConfigContext';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { PublicationInit } from '../../types/Publication';
import { AssetTag } from '../../types/AssetTag';
import { useFetch } from '../../hooks/useFetch';
import { handleErrors } from '../../util/cFetch';
import { Datepicker } from '../../components/Datepicker';


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
  const { response: wrenchTags } = useFetch<AssetTag[]>(`${serviceUrl}worker/rest/api/assets/any-tags/wrench-tags`);
  const { response: contentTags } = useFetch<AssetTag[]>(`${serviceUrl}worker/rest/api/assets/any-tags/stencil-tags`);

  const handleClose = () => {
    setOpen(false);
  }

  const handleSubmit = (assetReleaseCommand: PublicationInit): void => {
    let method = 'POST';
    let url = `${serviceUrl}worker/rest/api/assets/publications`;

    let init: PublicationInit = { ...assetReleaseCommand }
    // clear markers for new release creation
    if (assetReleaseCommand.stencilTag === NEW_TAG_VALUE) {
      init.stencilTag = null;
    }
    if (assetReleaseCommand.wrenchTag === NEW_TAG_VALUE) {
      init.wrenchTag = null;
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
        fullWidth InputProps={{ margin: 'dense' }}>
        <MenuItem key='-1' value={NEW_TAG_VALUE}>{intl.formatMessage({ id: 'publications.createNewTag' }, { tag: newTag })}</MenuItem>
        {
          tags?.map(tag => <MenuItem key={tag.name} value={tag.name}>{tag.name} / {tag.description}</MenuItem>)
        }
      </Field>
    )

  return (
    <>

      <Dialog open={open} onClose={handleClose} aria-labelledby='new-form-dialog-title' maxWidth='md' fullWidth>
        <DialogTitle fontWeight='bold' id='new-form-dialog-title'>{intl.formatMessage({ id: 'publications.dialogTitle' })}</DialogTitle>

        <Formik
          initialValues={{
            name: '',
            description: '',
            liveDate: null,
            wrenchTag: NEW_TAG_VALUE,
            stencilTag: NEW_TAG_VALUE
          }}
          enableReinitialize={true}
          onSubmit={(values, { setSubmitting }) => {
            handleSubmit(values);
            setSubmitting(false);
          }}
        >
          {
            ({ submitForm, isSubmitting, values, errors, isValid }) => (
              <Form>
                <DialogContent>
                  <Stack spacing={1}>
                    <Field
                      name='liveDate'
                      component={Datepicker}
                      disableMaskedInput
                      label={intl.formatMessage({ id: 'publications.liveDate' })}
                      fullWidth
                    />

                    <Field component={TextField} name='name' label={intl.formatMessage({ id: 'publications.name' })}
                      fullWidth required validate={requiredValidator} error={!!errors?.name}
                      helperText={errors?.name} InputProps={{ margin: 'dense' }} />


                    <Field component={TextField} name='description' label={intl.formatMessage({ id: 'publications.description' })} fullWidth InputProps={{ margin: 'dense' }} />

                    <TagComponent name='stencilTag' labelId='publications.contentTag' newTag={values.name} tags={contentTags} />
                    <TagComponent name='wrenchTag' labelId='publications.wrenchTag' newTag={values.name} tags={wrenchTags} />
                  </Stack>
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