import React from 'react';

import { Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Stack, Button, TextField } from '@mui/material';

import { useIntl, FormattedMessage } from 'react-intl';
import { useFetch } from '@dxs-ts/eveli-fetch';

import { PublicationApi } from '../api-publications'
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { DateTime } from 'luxon';



const NEW_TAG_VALUE = '-1';

export interface NewReleaseProps {
  onSubmit: () => void;
  open: boolean;
  setOpen: (open: boolean) => void
}

export const NewPublicationDialog: React.FC<NewReleaseProps> = ({ onSubmit, open, setOpen }) => {
  const intl = useIntl();
  const { wrenchTags } = useFetch('worker/rest/api/assets/any-tags/wrench-tags.GET', {});
  const { contentTags } = useFetch('worker/rest/api/assets/any-tags/stencil-tags.GET', {});
  const { savePublication } = useFetch('worker/rest/api/assets/publications.POST', {});
  const [isSubmitting, setSubmitting] = React.useState<boolean>(false);
  
  const [form, setForm] = React.useState<PublicationApi.PublicationInit>({
    name: '',
    description: '',
    liveDate: null,
    wrenchTag: NEW_TAG_VALUE,
    stencilTag: NEW_TAG_VALUE
  });

  const isValid = (
    // required fields
    !!form.name

  );

  const handleClose = () => {
    setOpen(false);
  }

  const handleSubmit = (): void => {
    setSubmitting(true)
    let init: PublicationApi.PublicationInit = { ...form }
    // clear markers for new release creation
    if (init.stencilTag === NEW_TAG_VALUE) {
      init.stencilTag = null;
    }
    if (init.wrenchTag === NEW_TAG_VALUE) {
      init.wrenchTag = null;
    }

    savePublication(init, () => {
      setOpen(false);
      onSubmit();
      setSubmitting(false)
    });
  }

  const TagComponent: React.FC<{ name: 'wrenchTag' | 'stencilTag', labelId: string, tags?: PublicationApi.AssetTag[], newTag: string }> =
    ({ name, labelId, tags, newTag }) => (
      <TextField select 
        name={name} 
        value={form[name] ?? NEW_TAG_VALUE}
        label={intl.formatMessage({ id: labelId })}
        fullWidth 
        slotProps={{ input: { margin: 'dense' } }}
        onChange={(event) => {
          const newValue = event.target.value;
          setForm(prev => {
            const next = {...prev};
            next[name] = newValue;
            return next;
          }) 
        }}>
        
        <MenuItem key='-1' value={NEW_TAG_VALUE}>{intl.formatMessage({ id: 'publications.createNewTag' }, { tag: newTag })}</MenuItem>
        { tags?.map(tag => <MenuItem key={tag.name} value={tag.name}>{tag.name} / {tag.description}</MenuItem>) }
      </TextField>
    )

  return (
      <Dialog open={open} onClose={handleClose} aria-labelledby='new-form-dialog-title' maxWidth='md' fullWidth>
        <DialogTitle fontWeight='bold' id='new-form-dialog-title'>{intl.formatMessage({ id: 'publications.dialogTitle' })}</DialogTitle>

        <DialogContent>
          <Stack spacing={1}>
            <DatePicker
              format='dd.MM.yyyy'
              value={form.liveDate ? DateTime.fromISO(form.liveDate).toJSDate() : null}
              label={intl.formatMessage({ id: 'publications.liveDate' })}
              slots={{textField: textFieldProps => <TextField fullWidth {...textFieldProps} />}}
              onChange={date => setForm(prev => {
                const next = {...prev};
                next.liveDate = date ? DateTime.fromJSDate(date).plus({ seconds: 1}).toLocal().toISO({ includeOffset: false,  }) : null;
                return next;
              })}
            />

            <TextField fullWidth required name='name' 
              label={intl.formatMessage({ id: 'publications.name' })}
              error={!form.name}
              helperText={!!form.name ? null : intl.formatMessage({ id: 'error.valueRequired'})} 
              slotProps={{ input: { margin: 'dense' } }}
              onChange={element => {
                const newValue = element.target.value;

                setForm(prev => {
                  const next = {...prev};
                  next.name = newValue;
                  return next;
                })
            }}
            />

            <TextField 
              name='description' 
              label={intl.formatMessage({ id: 'publications.description' })} 
              fullWidth
              slotProps={{ input: { margin: 'dense' } }}
              onChange={element => {
                const newValue = element.target.value;
                setForm(prev => {
                  const next = {...prev};
                  next.description = newValue;
                  return next;
                })
              }}
            />

            <TagComponent name='stencilTag' labelId='publications.contentTag' newTag={form.name} tags={contentTags} />
            <TagComponent name='wrenchTag' labelId='publications.wrenchTag' newTag={form.name} tags={wrenchTags} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}  variant='text'><FormattedMessage id='button.cancel'/></Button>
          <Button variant='contained' onClick={handleSubmit} disabled={isSubmitting || !isValid}>
            <FormattedMessage id='button.accept'/>
          </Button>
        </DialogActions>
      </Dialog>
  );
}