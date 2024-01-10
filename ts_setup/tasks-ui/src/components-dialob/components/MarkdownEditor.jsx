import React, {useState, useRef} from 'react';
import { TextArea, Segment, Button, Icon, Popup } from 'semantic-ui-react';
import Markdown from 'react-markdown';
import debounce from 'lodash.debounce';

export const MarkdownEditor = ({value, onChange}) => {
  const [preview, setPreview] = useState(false);
  const [content, setContent] = useState(value);
  const handleChange = useRef(debounce(value => {
    if (onChange) {
      onChange(value);
    }
  }, 250)).current;

  const updateValue = (value) => {
    setContent(value);
    handleChange(value);
  }

  return (
    <Segment.Group>
      <Segment padded={false} className='composer-md-editor-toolbar' basic>
        <Button.Group icon size='mini'>
          <Popup trigger={
          <Button active={!preview} onClick={() => setPreview(false)}>
            <Icon name='code'/>
          </Button>
          } on='hover' content='Edit markdown' />
          <Popup trigger={
         <Button active={preview} onClick={() => setPreview(true)}>
            <Icon name='eye'/>
          </Button>
          } on='hover' content='Preview' />
        </Button.Group>
        <Button.Group floated='right' icon size='mini'>
          <Button onClick={() => window.open('https://www.markdownguide.org/basic-syntax/')}>
            <Icon name='help' />
          </Button>
        </Button.Group>
      </Segment>
      <Segment className='composer-md-editor-content'>
        {
           preview
            ?
              <Markdown source={content} escapeHtml />
            :
              <TextArea value={content} onChange={(e) => updateValue(e.target.value)} className='composer-md-editor'/>
        }
      </Segment>
    </Segment.Group>
  );
}