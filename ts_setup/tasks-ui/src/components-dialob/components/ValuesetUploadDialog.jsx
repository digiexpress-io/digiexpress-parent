import React, { useState, createRef } from 'react';
import { Modal, Button, Grid, Form, Message, Icon } from 'semantic-ui-react';
import Papa from 'papaparse';

const ULMODE_REPLACE = 'replace';
const ULMODE_APPEND = 'append';
const ULMODE_UPDATE = 'update';

const uploadModeOptions = [
  { text: 'Replace all', value: ULMODE_REPLACE },
  { text: 'Append', value: ULMODE_APPEND },
  { text: 'Update', value: ULMODE_UPDATE }
];

const uploadModeDescriptions = {};
uploadModeDescriptions[ULMODE_REPLACE] = 'Replaces all valueset entries with values from file';
uploadModeDescriptions[ULMODE_APPEND] = 'Appends values from file to existing valueset entries';
uploadModeDescriptions[ULMODE_UPDATE] = 'Updates existing entries by ID and adds new entries from file';

const defaultUploadMode = ULMODE_REPLACE;

export const ValuesetUploadDialog = ({entries, setEntries}) => {
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadMode, setUploadMode] = useState(defaultUploadMode);
  const [error, setError] = useState(null);
  const fileInputRef = createRef();

  const fileChange = (event) => {
    setError(null);
    setSelectedFile(event.target.files[0]);
  };

  const handleClose = () => {
    setSelectedFile(null);
    setUploadMode(defaultUploadMode);
    setOpen(false);
    setLoading(false);
    setError(null);
  };

  const parse = (inputFile) => {
    return new Promise((resolve, reject) => {
      Papa.parse(inputFile, {
        header: true,
        transformHeader: h => h.trim(),
        skipEmptyLines: true,
        error: (error) => {
          console.error('CSV Parse error', error);
          reject(error);
        },
        complete: (results) => {
          resolve(results);
        }
      });
    });
  };

  const handleUpload = async function () {
    setLoading(true);
    setError(null);
    const csvResult = await parse(selectedFile);
    const t = csvResult.meta.fields[0];
    // Data validation
    if (
      csvResult.meta.fields.length < 2 ||
      csvResult.meta.fields[0] !== 'ID' ||
      csvResult.meta.fields.filter(f => f.length !== 2).length !== 0 ||
      csvResult.data.length === 0
    ) {
      setError('Invalid CSV format');
    } else {
      const newEntries = csvResult.data.map(d => {
        let label = {};
        csvResult.meta.fields.filter(f => f !== 'ID').forEach(f => {
          label[f] = d[f];
        });

        return ({
          id: d.ID.trim(),
          label
        });
      });

      // Apply
      if (uploadMode === ULMODE_REPLACE) {
        setEntries(newEntries);
      } else if (uploadMode === ULMODE_APPEND) {
        setEntries(entries.toJS().concat(newEntries));
      } else if (uploadMode === ULMODE_UPDATE) {
        let appliedEntries = entries.toJS();
        newEntries.forEach(e => {
          const idx = appliedEntries.findIndex(i => i.id === e.id);
          if (idx === -1) {
            appliedEntries.push(e);
          } else {
            appliedEntries[idx].label = e.label;
          }
        });
        setEntries(appliedEntries);
      }
      handleClose();
    }
    setLoading(false);
  };

  return (
    <React.Fragment>
      <Button size='tiny' icon='upload' onClick={() => setOpen(!open)} />
      <Modal open={open} size='small'>
        <Modal.Header>Upload valueset</Modal.Header>
        <Modal.Content>
          <Form>
            <Form.Field>
              <Grid>
             <Grid.Column width={6}>
                <Button
                  content='Choose CSV File'
                  labelPosition='left'
                  icon='file'
                  onClick={() => fileInputRef.current.click()}
                />
                <input
                  ref={fileInputRef}
                  type='file'
                  accept='text/csv'
                  hidden
                  onChange={fileChange}
                />
              </Grid.Column>
              <Grid.Column width={6}>
                <strong>{selectedFile && selectedFile.name}</strong>
              </Grid.Column>
            </Grid>
            </Form.Field>
          <Form.Field>
            <Form.Select fluid label='Upload mode' options={uploadModeOptions} value={uploadMode} onChange={(_, data) => setUploadMode(data.value)}/>
            <Message>
              <p>{uploadModeDescriptions[uploadMode]}</p>
            </Message>
          </Form.Field>
          { error &&
            <Form.Field>
              <Message negative icon>
                <Icon name='exclamation triangle' />
                <Message.Content>
                  <Message.Header>Import failed</Message.Header>
                  <p>{error}</p>
                </Message.Content>
              </Message>
            </Form.Field>
          }
          </Form>
        </Modal.Content>
      <Modal.Actions>
        <Button onClick={handleUpload} primary icon='upload' disabled={!selectedFile} loading={loading} content='Upload' />
        <Button onClick={handleClose}>Cancel</Button>
      </Modal.Actions>
      </Modal>
    </React.Fragment >
  );

};

