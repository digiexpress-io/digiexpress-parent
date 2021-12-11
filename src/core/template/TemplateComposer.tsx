import React from 'react';
import { Typography, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import MDEditor, { ICommand, getCommands } from '@uiw/react-md-editor';

import StencilStyles from '../styles';
import { Composer, StencilClient } from '../context';


interface TemplateComposerProps {
  onClose: () => void;
}

const TemplateComposer: React.FC<TemplateComposerProps> = ({ onClose }) => {
  const [name, setName] = React.useState('');
  const [description, setDescription] = React.useState('');
  const [content, setContent] = React.useState('');
  const [templateType, setTemplateType] = React.useState<'page' | string>('page');
  const { service, actions} = Composer.useComposer();

  const handleCreate = () => {
    const entity: StencilClient.CreateTemplate = {
      content, description, name, templateType
    };
    service.create().template(entity).then(success => {
      console.log(success, entity);
      onClose();
      actions.handleLoadSite();
    })
  }
  const handleContentChange = (value: string | undefined) => {
    setContent(value ? value : '')
  }

  return (
    <StencilStyles.Dialog open={true} onClose={onClose}
      title={"template.create"}
      backgroundColor={"uiElements.main"}
      submit={{ title: "button.add", disabled: !name || !content, onClick: handleCreate }}
    >

      <>
        <Typography variant="body2" ><FormattedMessage id={"template.description"} /></Typography>

        <StencilStyles.TextField label='template.name' helperText='template.name.desc'
          value={name}
          onChange={setName} />

        <StencilStyles.Select label='template.type'
          selected={templateType}
          onChange={setTemplateType}
          helperText={"template.page.desc"}

          items={[
            { id: 'page', value: <FormattedMessage id='template.page' /> },
          ]} />


        <StencilStyles.TextField label='template.desc' helperText='template.description.desc'
          value={description}
          onChange={setDescription} />

        <Typography variant="body2" sx={{ mt: 2, fontWeight: 'bold' }}><FormattedMessage id={"templates.intro"} /></Typography>

        <Box display="flex" flexDirection="row" flexWrap="wrap" sx={{ mt: 2 }}>
          <Box flex="1" sx={{ paddingRight: 1 }}>
            <MDEditor key={1} value={content} onChange={handleContentChange}
              textareaProps={{ placeholder: '# Level 1 Header' }}
              height={300}
            />
          </Box>
        </Box>
      </>
    </StencilStyles.Dialog >
  );
}

export { TemplateComposer }