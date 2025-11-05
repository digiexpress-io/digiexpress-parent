import React from 'react';
import { Typography, Button, Box, Dialog, DialogTitle, DialogContent, DialogActions, FormHelperText } from '@mui/material';
import { useSnackbar } from 'notistack';

import { FormattedMessage, useIntl } from 'react-intl';
import MDEditor from '@uiw/react-md-editor';

import * as Burger from '@dxs-ts/eveli-primitives';
import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { TemplateComposerRoot, useTemplateComposerUtilityClasses } from './useUtilityClasses';


interface TemplateComposerProps {
  onClose: () => void;
}

const TemplateComposer: React.FC<TemplateComposerProps> = ({ onClose }) => {
  const classes = useTemplateComposerUtilityClasses();
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();
  const [name, setName] = React.useState('');
  const [description, setDescription] = React.useState('');
  const [content, setContent] = React.useState('');
  const [templateType, setTemplateType] = React.useState<'page' | string>('page');
  const { service, actions } = Composer.useComposer();

  const handleCreate = () => {
    const entity: StencilApi.CreateTemplate = {
      content, description, name, type: templateType
    };
    service.create().template(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success, entity);
      onClose();
      actions.handleLoadSite();
    })
  }
  const handleContentChange = (value: string | undefined) => {
    setContent(value ? value : '')
  }
  const message = <FormattedMessage id="snack.template.createdMessage" />


  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='template.create' /></DialogTitle>
      <DialogContent>
        <TemplateComposerRoot className={classes.root}>
          <Typography variant="body2"><FormattedMessage id={"template.description"} /></Typography>
          <Burger.TextField
            label='template.name'
            value={name}
            onChange={setName}
          />
          <Box>
            {!name && <FormHelperText error className={classes.helperText}>{intl.formatMessage({ id: 'error.valueRequired' })}</FormHelperText>}
          </Box>

          <Burger.Select label='template.type'
            selected={templateType}
            onChange={setTemplateType}
            helperText="template.page.desc"
            items={[{ id: 'page', value: <FormattedMessage id='template.page' /> }]}
          />

          <Burger.TextField label='template.desc' helperText='template.description.desc'
            value={description}
            onChange={setDescription} />

          <Typography variant="body2" className={classes.sectionTitle}>
            <FormattedMessage id={"templates.intro"} />
          </Typography>

          <Box className={classes.editorRow}>
            <Box className={classes.editorCol}>
              <MDEditor key={1} value={content} onChange={handleContentChange}
                textareaProps={{ placeholder: '# Level 1 Header' }}
                height={300}
              />
            </Box>
          </Box>
        </TemplateComposerRoot>
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleCreate} disabled={!name || !content}>
          <FormattedMessage id='button.add' />
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export { TemplateComposer }