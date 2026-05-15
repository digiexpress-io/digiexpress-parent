import React from 'react';
import {
  Box, TableContainer, Typography, Table, TableBody, RadioGroup, FormControlLabel,
  Button, Checkbox, Radio, Dialog, DialogTitle, DialogContent, DialogActions
} from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { DebugDrawer } from './drawer/DebugDrawer';
import { SelectAsset } from './drawer/SelectAsset';
import { InputCSV } from './drawer/InputCSV';
import { InputJSON } from './drawer/InputJSON';
import { InputFORM } from './drawer/InputFORM';

import { DebugError } from './DebugError';
import { DebugHeader } from './DebugHeader';
import { DebugInput } from './DebugInput';
import { DebugOutput } from './outputs/DebugOutput';
import { DebugOptionType } from './api';


type LocalDebugSession = {
  json: string;
  csv: string;
  inputType: Fs.DebugInputType;
  debug?: Fs.DebugResponse;
  error?: Fs.StoreError;
};

const DebugView: React.FC<{}> = ({ }) => {
  const [option, setOption] = React.useState<DebugOptionType | undefined>();
  const [selectedId, setSelectedId] = React.useState<string | undefined>();
  const [selectedDirent, setSelectedDirent] = React.useState<Fs.DirentBase | undefined>();
  const [session, setSession] = React.useState<LocalDebugSession>({ json: '{}', csv: '', inputType: 'JSON' });
  const [sessions, setSessions] = React.useState<Record<string, LocalDebugSession>>({});
  const [dialogShow, setDialogShow] = React.useState(false);
  const [delimiter, setDelimiter] = React.useState("semicolon");
  const [wrap, setWrap] = React.useState(false);
  const intl = useIntl();
  const { debugDirent } = useFsDirent();

  const handleCsv = (csv: string) => {
    const updated = { ...session, inputType: 'CSV' as Fs.DebugInputType, csv };
    setSession(updated);
    if (selectedId) {
      setSessions(prev => ({ ...prev, [selectedId]: updated }));
    }
  }
  const handleJson = (input: object) => {
    const updated = { ...session, inputType: 'JSON' as Fs.DebugInputType, json: JSON.stringify(input) };
    setSession(updated);
    if (selectedId) {
      setSessions(prev => ({ ...prev, [selectedId]: updated }));
    }
  }
  const handleSelectAsset = (dirent: Fs.DirentBase) => {
    const existing = sessions[dirent.id];
    setSelectedId(dirent.id);
    setSelectedDirent(dirent);
    if (existing) {
      setSession(existing);
      setOption(undefined);
    } else {
      setSession({ json: '{}', csv: '', inputType: 'JSON' });
      setOption('INPUT_FORM');
    }
  }

  const handleExecute = () => {
    if (!selectedId) {
      return;
    }
    debugDirent({
      id: selectedId,
      input: session.inputType === 'JSON' ? session.json : undefined,
      inputCSV: session.inputType === 'CSV' ? session.csv : undefined
    })
      .then(response => {
        const updated = { ...session, debug: response, error: undefined };
        setSession(updated);
        setSessions(prev => ({ ...prev, [selectedId]: updated }));
      })
      .catch(error => {
        const updated = { ...session, error, debug: undefined };
        setSession(updated);
        setSessions(prev => ({ ...prev, [selectedId]: updated }));
      });
  }

  const downloadCsv = (delimiter: string, wrap: boolean) => {
    var content: string = session.debug?.bodyCsv ? session.debug?.bodyCsv : "";
    if (content.includes('\r')) {
      content = content.replace(/\r/g, '');
    }
    if (content.endsWith("\n")) {
      content = content.substring(0, content.length - 1);
    }
    const lines = content.split('\n');
    const outputHeaders = lines[0].split(',');
    const outputLines = lines.slice(1, lines.length/2);
    const inputHeaders = lines[lines.length/2].split(';');
    const inputLines = lines.slice(lines.length/2+1, lines.length);
    var finalContent = "";
    if (!wrap) {
      finalContent = delimiter === "comma" ?
        formatCsvForDownloadWithComma(outputHeaders, outputLines, inputHeaders, inputLines) :
        formatCsvForDownloadWithSemicolon(outputHeaders, outputLines, inputHeaders, inputLines);
    } else {
      finalContent = delimiter === "comma" ?
        wrapAndComma(outputHeaders, outputLines, inputHeaders, inputLines) :
        wrapAndSemicolon(outputHeaders, outputLines, inputHeaders, inputLines);
    }
    var blob = new Blob([finalContent], { type: 'text/csv' });
    var url = window.URL.createObjectURL(blob);
    var pom = document.createElement('a');
    pom.href = url;
    pom.setAttribute('download', 'results.csv');
    pom.click();
  }

  const formatCsvForDownloadWithSemicolon = (outputHeaders: string[], outputLines: string[], inputHeaders: string[], inputLines: string[]) => {
    const finalHeaders = inputHeaders.concat(outputHeaders);
    const finalLines = inputLines.map((inputLine, index) => {
      const outputLine = outputLines[index].replace(/,/g, ';');
      return inputLine.concat(';').concat(outputLine);
    });
    const finalContent = finalHeaders.join(';').concat('\n').concat(finalLines.join('\n'));
    return finalContent;
  }

  const formatCsvForDownloadWithComma = (outputHeaders: string[], outputLines: string[], inputHeaders: string[], inputLines: string[]) => {
    const finalHeaders = inputHeaders.concat(outputHeaders);
    const finalLines = inputLines.map((inputLine, index) => {
      const outputLine = outputLines[index];
      return inputLine.replace(';',',').concat(',').concat(outputLine);
    });
    const finalContent = finalHeaders.join(',').concat('\n').concat(finalLines.join('\n'));
    return finalContent;
  }

  const wrapAndComma = (outputHeaders: string[], outputLines: string[], inputHeaders: string[], inputLines: string[]) => {
    const combinedHeaders = inputHeaders.concat(outputHeaders);
    const combinedLines = inputLines.map((inputLine, index) => {
      const outputLine = outputLines[index];
      const outputValues = outputLine.split(',');
      // handling cases where output contains a comma
      for (let j = 0; j < outputValues.length; j++) {
          const outputValue = outputValues[j];
          if (outputValue.startsWith('"') ) {
              for (let k = j+1; k < outputValues.length; k++) {
                  const outputValue2 = outputValues[k];
                  if (outputValue2.endsWith('"')) {
                      outputValues[j] = outputValues.slice(j, k+1).join(',').slice(1, -1);
                      outputValues.splice(j+1, k-j);
                      break;
                  }
              }
          }
      }
      // wrap output values
      for (let j = 0; j < outputValues.length; j++) {
          const outputValue = outputValues[j];
          if (outputValue.includes(',')) {
              outputValues[j] = '"' + outputValue + '"';
          }
      }
      // wrap input values
      const inputValues = inputLine.split(';');
      for (let j = 0; j < inputValues.length; j++) {
          const inputValue = inputValues[j];
          if (inputValue.includes(',')) {
              inputValues[j] = '"' + inputValue + '"';
          }
      }
      return inputValues.concat(outputValues).join(',');
    });
    return combinedHeaders.join(',').concat('\n').concat(combinedLines.join('\n'));
  }

  const wrapAndSemicolon = (outputHeaders: string[], outputLines: string[], inputHeaders: string[], inputLines: string[]) => {
    const combinedHeaders = inputHeaders.concat(outputHeaders);
    const combinedLines = inputLines.map((inputLine, index) => {
      const outputLine = outputLines[index];
      const outputValues = outputLine.split(',');
      // handling cases where output contains a comma
      for (let j = 0; j < outputValues.length; j++) {
        const outputValue = outputValues[j];
        if (outputValue.startsWith('"') && !outputValue.includes(';') ) {
            for (let k = j+1; k < outputValues.length; k++) {
                const outputValue2 = outputValues[k];
                if (outputValue2.endsWith('"')) {
                    outputValues[j] = outputValues.slice(j, k+1).join(',').slice(1, -1);
                    outputValues.splice(j+1, k-j);
                    break;
                }
            }
        }
      // wrap output values
      for (let l = 0; l < outputValues.length; l++) {
       const outputValue = outputValues[l];
       if (outputValue.includes(';') && !outputValue.startsWith('"')) {
           outputValues[l] = '"' + outputValue + '"';
       }
      }
      }
      return inputLine.concat(';').concat(outputValues.join(';'));
    });
    return combinedHeaders.join(';').concat('\n').concat(combinedLines.join('\n'));
  }

  const selected = selectedId ?? "";
  const json = session.json;
  const csv = session.csv;
  const inputType = session.inputType;
  const response = session.debug;
  const error = session.error;

  let dialogChildren = (
    <>
      <p><b>{intl.formatMessage({id: 'debug.csv.download.delimiter'})}</b></p>
      <RadioGroup
        aria-labelledby="demo-controlled-radio-buttons-group"
        name="controlled-radio-buttons-group"
        value={delimiter}
        onChange={(e) => setDelimiter(e.target.value)}
      >
        <FormControlLabel value="comma" control={<Radio />} label={intl.formatMessage({id: 'debug.csv.download.delimiter.comma'})} />
        <FormControlLabel value="semicolon" control={<Radio />} label={intl.formatMessage({id: 'debug.csv.download.delimiter.semicolon'})} />
      </RadioGroup>
      <p>{intl.formatMessage({id: 'debug.csv.download.options'})}</p>
      <Checkbox checked={wrap} onChange={() => setWrap(!wrap)} />
      <label>{intl.formatMessage({ id: 'debug.csv.download.wrap' })}</label>
    </>
  );

  return (<Box sx={{ width: '100%', overflow: 'hidden', padding: 1 }}>
    <Typography variant='h1'>{intl.formatMessage({ id: 'main.debug' })}</Typography>

    <DebugDrawer selected={selected} open={option === "DRAWER"} onClose={() => setOption(undefined)} onSelect={setOption} />

    {option === 'SELECT_ASSET' ? <SelectAsset onClose={() => setOption(undefined)} selected={selected} onSelect={handleSelectAsset} /> : null}
    {option === 'INPUT_JSON' && json ? <InputJSON onClose={() => setOption(undefined)} onSelect={handleJson} value={json} /> : null}
    {option === 'INPUT_FORM' && json ? <InputFORM onClose={() => setOption(undefined)} selectedDirent={selectedDirent} onSelect={handleJson} value={json} /> : null}
    {option === 'INPUT_CSV' ? <InputCSV onClose={() => setOption(undefined)} selected={selected} onSelect={handleCsv} value={csv} /> : null}


    <TableContainer sx={{ height: "calc(100vh - 150px)" }}>
      <Table stickyHeader size="small">
        <DebugHeader>
          {selectedDirent ?
            (<Button variant='contained' children={`${selectedDirent?.type} - ${selectedDirent?.name}`} onClick={() => setOption('SELECT_ASSET')} />) :
            (<Button variant='contained' onClick={() => setOption('SELECT_ASSET')} >{intl.formatMessage({ id: 'debug.toolbar.noAsset' })}</Button>)
          }
          {/* TODO: open asset navigation not yet implemented */}
          <Button variant='contained' disabled={true} sx={{ ml: 1 }} >{intl.formatMessage({ id: 'debug.toolbar.openAsset' })}</Button>
          <Button variant='contained' onClick={() => setOption('DRAWER')} sx={{ ml: 1 }} >{intl.formatMessage({ id: 'debug.toolbar.options' })}</Button>
          <Button variant='contained' disabled={selected ? false : true} onClick={() => handleExecute()} sx={{ ml: 1 }} >{intl.formatMessage({ id: 'debug.toolbar.execute' })}</Button>
          {inputType === 'CSV' && session.debug?.bodyCsv ? <Button variant='contained' disabled={selected ? false : true} onClick={() => setDialogShow(true)} sx={{ ml: 1 }} >{intl.formatMessage({ id: 'debug.toolbar.download' })}</Button> : null}
        </DebugHeader>

        <TableBody>
          {json ? <DebugInput type={inputType} csv={csv} json={json} /> : null}
          {error ? <DebugError error={error} /> : null}
          {response ? <DebugOutput debug={response} selected={selectedDirent} /> : null}
        </TableBody>
      </Table>
    </TableContainer>

    { dialogShow && (
      <Dialog open={true} onClose={() => setDialogShow(false)}>
        <DialogTitle>{intl.formatMessage({ id: 'debug.csv.download' })}</DialogTitle>
        <DialogContent>{dialogChildren}</DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogShow(false)}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
          <Button onClick={() => downloadCsv(delimiter, wrap)}>
            {intl.formatMessage({ id: 'buttons.download' })}
          </Button>
        </DialogActions>
      </Dialog>
    )}

  </Box >);
}

export { DebugView };
