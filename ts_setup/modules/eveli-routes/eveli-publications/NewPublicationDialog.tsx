import React from 'react';

import {
  Dialog, DialogActions, DialogContent, DialogTitle,
  MenuItem, Stack, Button, TextField, FormLabel,
  useTheme
} from '@mui/material';

import { DateTime } from 'luxon';
import { useIntl, FormattedMessage } from 'react-intl';

import { useFetch } from '@dxs-ts/envir-fetch';
import { PublicationApi } from '@dxs-ts/eveli-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { DatePicker } from '@dxs-ts/xui-datetime';

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
  const theme = useTheme();

  const [form, setForm] = React.useState<PublicationApi.PublicationInit>({
    name: '',
    description: '',
    liveDate: null,
    wrenchTag: NEW_TAG_VALUE,
    stencilTag: NEW_TAG_VALUE
  });

  const isValid = !!form.name;

  const handleClose = () => setOpen(false);

  const handleSubmit = (): void => {
    setSubmitting(true);
    const init: PublicationApi.PublicationInit = {
      ...form,
      stencilTag: form.stencilTag === NEW_TAG_VALUE ? null : form.stencilTag,
      wrenchTag: form.wrenchTag === NEW_TAG_VALUE ? null : form.wrenchTag,
    };

    savePublication(init, () => {
      setOpen(false);
      onSubmit();
      setSubmitting(false);
    });
  };

  const TagComponent: React.FC<{
    name: 'wrenchTag' | 'stencilTag',
    labelId: string,
    tags?: PublicationApi.AssetTag[],
    newTag: string
  }> = ({ name, labelId, tags, newTag }) => (

    <TextField
      select
      name={name}
      value={form[name] ?? NEW_TAG_VALUE}
      label={intl.formatMessage({ id: labelId })}
      fullWidth
      slotProps={{ input: { margin: 'dense' } }}
      sx={{ minHeight: '72px' }}
      onChange={(event) => {
        const newValue = event.target.value;
        setForm(prev => ({ ...prev, [name]: newValue }));
      }}
    >
      <MenuItem key='-1' value={NEW_TAG_VALUE}>
        {intl.formatMessage({ id: 'publications.createNewTag' }, { tag: newTag })}
      </MenuItem>
      {tags?.map(tag => (
        <MenuItem key={tag.name} value={tag.name}>
          {tag.name} / {tag.description}
        </MenuItem>
      ))}
    </TextField>
  );

  return (
    <Dialog open={open} onClose={handleClose} aria-labelledby='new-form-dialog-title' maxWidth='md' fullWidth>
      <DialogTitle fontWeight='bold' id='new-form-dialog-title'>
        {intl.formatMessage({ id: 'publications.dialogTitle' })}
      </DialogTitle>

      <DialogContent>
        <Stack spacing={1}>
          <FormLabel>{intl.formatMessage({ id: 'publications.liveDate' })}</FormLabel>
          <DatePicker 
            /* sx={{
               border: '2px solid transparent',
               ':focus-within': {
                 border: `2px solid ${theme.palette.primary.main}`,
                 borderRadius: theme.spacing(0.5),
                 '.XuiDatePicker-input': {
                   border: 'transparent'
                 }
               }
             }}
               */
            fullWidth value={form.liveDate ? DateTime.fromISO(form.liveDate).toJSDate() : null}
            onChange={(date) =>
              setForm(prev => ({
                ...prev,
                liveDate: date
                  ? DateTime.fromJSDate(date).plus({ seconds: 1 }).toLocal()
                    .toISO({ includeOffset: false })
                  : null
              }))
            }
          />

          <TextField
            fullWidth
            required
            name='name'
            label={intl.formatMessage({ id: 'publications.name' })}
            error={!form.name}
            helperText={!form.name ? intl.formatMessage({ id: 'error.valueRequired' }) : ' '}
            slotProps={{ input: { margin: 'dense' } }}
            sx={{ position: 'relative', minHeight: '72px' }}
            FormHelperTextProps={{ sx: { position: 'absolute', bottom: 0, left: 0, lineHeight: 1.2, m: 0 } }}
            onChange={e => setForm(prev => ({ ...prev, name: e.target.value }))}
          />

          <TextField
            name='description'
            label={intl.formatMessage({ id: 'publications.description' })}
            fullWidth
            slotProps={{ input: { margin: 'dense' } }}
            sx={{ minHeight: '72px' }}
            onChange={e => setForm(prev => ({ ...prev, description: e.target.value }))}
          />

          <TagComponent name='stencilTag' labelId='publications.contentTag' newTag={form.name} tags={contentTags} />
          <TagComponent name='wrenchTag' labelId='publications.wrenchTag' newTag={form.name} tags={wrenchTags} />
        </Stack>
      </DialogContent>

      <DialogActions>
        <CancelButton onClick={handleClose} />
        <Button variant='contained' onClick={handleSubmit} disabled={isSubmitting || !isValid}>
          <FormattedMessage id='button.accept' />
        </Button>
      </DialogActions>
    </Dialog>
  );
};
