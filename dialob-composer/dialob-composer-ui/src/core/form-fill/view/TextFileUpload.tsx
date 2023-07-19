import React, { SyntheticEvent } from 'react';
import { createStyles, makeStyles } from '@mui/styles';

import {
  TextField, Theme, TableCell, Table, TableRow, TableBody,
  TableHead, IconButton, Tooltip, Typography, CircularProgress
} from '@mui/material';

import { ItemAction, SessionError } from '@dialob/fill-api';
import { useFillActions } from '@dialob/fill-react';
import { FormattedMessage } from 'react-intl';

import AddIcon from '@mui/icons-material/Add';
import ClearIcon from '@mui/icons-material/Clear';

import { DescriptionWrapper, RenderErrors } from '@dialob/fill-material';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    addButton: {
      marginLeft: theme.spacing(1)
    },
    button: {
      marginLeft: theme.spacing(1),
      color: theme.palette.error.main
    },
    border: {
      borderBottom: 'none',
      paddingBottom: 0,
    },
    mouseOver: {
      pointerEvents: 'none'
    },

  }),
);


export interface TextFileUploadProps {
  text: ItemAction<any, any, string>['item'];
  errors: SessionError[];
  onAttachments: (files: FileList) => Promise<void>;
};
export const TextFileUpload: React.FC<TextFileUploadProps> = ({ text, errors, onAttachments }) => {
  const classes = useStyles();
  const { setAnswer } = useFillActions();
  const inputFile = React.useRef<HTMLInputElement>(null);
  const fileNames: string[] = text.value ? JSON.parse(text.value) : [];
  const [loading, setLoading] = React.useState(false);

  const handleFileUpload = (event: SyntheticEvent<HTMLInputElement>) => {
    setLoading(true);

    const files = event.currentTarget.files;
    if (!files || files.length === 0) {
      return;
    }
    const names: string[] = [...fileNames];
    for (const file of Array.from(files)) {
      if (!names.includes(file.name)) {
        names.push(file.name);
      }
    }
    onAttachments(files)
      .then(() => setAnswer(text.id, JSON.stringify(names)))
      .then(() => setLoading(false));
  }

  const handleFileDelete = (index: number) => {
    const names: string[] = [...fileNames];
    names.splice(index, 1)
    setAnswer(text.id, JSON.stringify(names));
  }

  return (
    <Table>
      <TableHead>
        <TableRow>
          <DescriptionWrapper text={text.description} title={text.label} />
          <TableCell>
            <Typography variant="h4"><FormattedMessage id="attachment.title" /></Typography>
            <RenderErrors errors={errors} />
          </TableCell>
          <TableCell align="right">

            <input type='file' id='file' multiple ref={inputFile} style={{ display: 'none' }} onChange={handleFileUpload} accept='.jpg, .jpeg, .png, .pdf' />
            <IconButton onClick={() => inputFile.current?.click()}>
              <AddIcon color="primary" />
            </IconButton>

          </TableCell>
        </TableRow>
      </TableHead>

      <TableBody>
        {fileNames.map((name, index) => (
          <TableRow key={index}>
            <TableCell className={classes.border}>
              <TextField
                fullWidth
                className={classes.mouseOver}
                inputProps={
                  { readOnly: true, }
                }
                label={<FormattedMessage id="attachment.fileName" values={{ index: index + 1 }} />}
                required={text.required}
                error={errors.length > 0}
                value={name}
              />
            </TableCell>
            <TableCell align="right" className={classes.border}>
              <Tooltip title={<FormattedMessage id="attachment.remove" />}>
                <IconButton className={classes.button} onClick={() => handleFileDelete(index)}>
                  <ClearIcon />
                </IconButton>
              </Tooltip>
            </TableCell>
          </TableRow>

        ))
        }
        {loading ? (<TableRow>
          <TableCell className={classes.border}>
            <CircularProgress color="inherit" />
          </TableCell>
        </TableRow>) : null}
      </TableBody>

    </Table>

  );
};
