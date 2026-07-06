import React from 'react';
import {
  Box, TableContainer, Typography, Table, TableBody, RadioGroup, FormControlLabel,
  Button, Checkbox, Radio, Dialog, DialogTitle, DialogContent, DialogActions
} from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { DebugDrawer } from './drawer/DebugDrawer';
import { InputCSV } from './drawer/InputCSV';
import { InputJSON } from './drawer/InputJSON';
import { InputFORM } from './drawer/InputFORM';

import { DebugError } from './DebugError';
import { DebugHeader } from './DebugHeader';
import { DebugInput } from './DebugInput';
import { DebugOutput } from './outputs/DebugOutput';
import { DebugOptionType } from './api';
import { downloadCsv } from './csv';


type LocalDebugSession = {
  json: string;
  csv: string;
  inputType: Fs.DebugInputType;
  debug?: Fs.DebugResponse;
  error?: Fs.StoreError;
}

export const FsPanelDebug: React.FC<{ dirent: Fs.DirentBase }> = ({ dirent }) => {
  const [option, setOption] = React.useState<DebugOptionType | undefined>();
  const [session, setSession] = React.useState<LocalDebugSession>({ json: '{}', csv: '', inputType: 'JSON' });
  const [dialogShow, setDialogShow] = React.useState(false);
  const [delimiter, setDelimiter] = React.useState("semicolon");
  const [wrap, setWrap] = React.useState(false);
  const intl = useIntl();
  const { debugDirent } = useFsDirent();

  const handleCsv = (csv: string) => {
    const updated = { ...session, inputType: 'CSV' as Fs.DebugInputType, csv };
    setSession(updated);
  }
  const handleJson = (input: object) => {
    const updated = { ...session, inputType: 'JSON' as Fs.DebugInputType, json: JSON.stringify(input) };
    setSession(updated);

  }

  const handleExecute = () => {
    debugDirent({
      id: dirent.id,
      input: session.inputType === 'JSON' ? session.json : undefined,
      inputCSV: session.inputType === 'CSV' ? session.csv : undefined
    })
      .then(response => {
        const updated = { ...session, debug: response, error: undefined };
        setSession(updated);
      })
      .catch(error => {
        const updated = { ...session, error, debug: undefined };
        setSession(updated);
      });
  }

  const json = session.json;
  const csv = session.csv;
  const inputType = session.inputType;
  const response = session.debug;
  const error = session.error;

  return (<Box sx={{ width: '100%', overflow: 'hidden', padding: 1 }}>
    <Typography variant='h1'>{intl.formatMessage({ id: 'main.debug' })}</Typography>



    {option === 'INPUT_JSON' && json ? <InputJSON onClose={() => setOption(undefined)} onSelect={handleJson} value={json} /> : null}
    {option === 'INPUT_FORM' && json ? <InputFORM onClose={() => setOption(undefined)} selectedDirent={dirent} onSelect={handleJson} value={json} /> : null}
    {option === 'INPUT_CSV' ? <InputCSV onClose={() => setOption(undefined)} onSelect={handleCsv} value={csv} /> : null}


    <TableContainer sx={{ height: "calc(100vh - 150px)" }}>
      <Table stickyHeader size="small">
        <DebugHeader>
          <DebugDrawer onSelect={setOption}/>
          <Button variant='contained' onClick={() => handleExecute()} sx={{ ml: 1 }} >{intl.formatMessage({ id: 'debug.toolbar.execute' })}</Button>
          <Button variant='contained' disabled={!(inputType === 'CSV' && session.debug?.bodyCsv)} onClick={() => setDialogShow(true)} sx={{ ml: 1 }} >{intl.formatMessage({ id: 'debug.toolbar.download' })}</Button>
        </DebugHeader>

        <TableBody>
          {json ? <DebugInput type={inputType} csv={csv} json={json} /> : null}
          {error ? <DebugError error={error} /> : null}
          {response ? <DebugOutput debug={response} selected={dirent} /> : null}
        </TableBody>
      </Table>
    </TableContainer>

    { dialogShow && (
      <Dialog open={true} onClose={() => setDialogShow(false)}>
        <DialogTitle>{intl.formatMessage({ id: 'debug.csv.download' })}</DialogTitle>
        <DialogContent>
          <p><b>{intl.formatMessage({ id: 'debug.csv.download.delimiter' })}</b></p>
          <RadioGroup
            aria-labelledby="demo-controlled-radio-buttons-group"
            name="controlled-radio-buttons-group"
            value={delimiter}
            onChange={(e) => setDelimiter(e.target.value)}
          >
            <FormControlLabel value="comma" control={<Radio />} label={intl.formatMessage({ id: 'debug.csv.download.delimiter.comma' })} />
            <FormControlLabel value="semicolon" control={<Radio />} label={intl.formatMessage({ id: 'debug.csv.download.delimiter.semicolon' })} />
          </RadioGroup>
          <p>{intl.formatMessage({ id: 'debug.csv.download.options' })}</p>
          <Checkbox checked={wrap} onChange={() => setWrap(!wrap)} />
          <label>{intl.formatMessage({ id: 'debug.csv.download.wrap' })}</label>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogShow(false)}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
          <Button onClick={() => downloadCsv(delimiter, wrap, session.debug?.bodyCsv)}>
            {intl.formatMessage({ id: 'buttons.download' })}
          </Button>
        </DialogActions>
      </Dialog>
    )}
  </Box >);
}
