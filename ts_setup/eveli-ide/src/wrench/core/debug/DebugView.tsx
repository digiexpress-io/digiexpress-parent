import React from 'react';
import { Box, TableContainer, Table, TableBody, RadioGroup, FormControlLabel } from '@mui/material';

import Burger from '@/burger';
import { Client, Composer } from '../context';

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
import { useIntl } from 'react-intl';


const getData = (session: Composer.Session): {
  ast?: Client.AstBody;
  entity?: Client.Entity<Client.AstBody>
  debug?: Composer.DebugSession;
  debugValues: string
} => {
  const entity = session.debug.selected ? session.getEntity(session.debug.selected) : undefined;
  const ast = entity ? entity?.ast : undefined;
  const debug: Composer.DebugSession | undefined = session.debug.selected ? session.debug.values[session.debug.selected] : undefined;

  const elements = ast ? ast.headers.acceptDefs : [];
  const data: any = {};
  for (const parameter of elements) {
    if (parameter.values !== undefined) {
      data[parameter.name] = parameter.values ? parameter.values : "";
    }
  }

  return { ast, debug, entity, debugValues: JSON.stringify(data) };
}


const DebugView: React.FC<{}> = ({ }) => {
  const { service, session, actions } = Composer.useComposer();
  const nav = Composer.useNav();
  const { ast, debug, entity, debugValues } = getData(session);
  const [option, setOption] = React.useState<DebugOptionType | undefined>();
  const [dialogShow, setDialogShow] = React.useState(false);
  const [delimiter, setDelimiter] = React.useState("semicolon");
  const [wrap, setWrap] = React.useState(false);
  const intl = useIntl();

  const selected = debug?.selected ? debug.selected : "";
  const json = debug?.json ? debug?.json : debugValues;
  const csv = debug?.csv ? debug.csv : "";
  const inputType = debug ? debug.inputType : "JSON";
  const response = debug?.debug;
  const error = debug?.error;

  const handleCsv = (csv: string) => {
    actions.handleDebugUpdate({ inputType: "CSV", csv, selected, debug: response, error, json })
  }
  const handleJson = (input: object) => {
    actions.handleDebugUpdate({ inputType: "JSON", csv, selected, debug: response, error, json: JSON.stringify(input) })
  }
  const handleSelectAsset = (selected: Client.Entity<Client.AstBody>) => {
    if (session.debug.selected && session.debug.selected !== selected.id) {
      const previous = session.debug.values[selected.id];
      if (previous) {
        setOption(undefined);
        actions.handleDebugUpdate(previous);
        return;
      }
    }

    actions.handleDebugUpdate({ inputType: "JSON", selected: selected.id })
    setOption("INPUT_FORM");
  }

  const handleExecute = () => {
    if (!selected) {
      return;
    }

    service.debug({
      id: selected,
      input: inputType === 'JSON' ? json : undefined,
      inputCSV: inputType === 'CSV' ? csv : undefined
    })
      .then(response => actions.handleDebugUpdate({ inputType, csv, selected, json, debug: response, error: undefined }))
      .catch(error => actions.handleDebugUpdate({ inputType, csv, selected, json, error, debug: undefined }))
  }

  const downloadCsv = (delimiter: string, wrap: boolean) => {
    var content : string = debug?.debug?.bodyCsv ? debug?.debug?.bodyCsv : "";
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

  let dialogChildren = (
    <>
      <p><b>{intl.formatMessage({id: 'debug.csv.download.delimiter'})}</b></p>
      <RadioGroup
        aria-labelledby="demo-controlled-radio-buttons-group"
        name="controlled-radio-buttons-group"
        value={delimiter}
        onChange={(e) => setDelimiter(e.target.value)}
      >
        <FormControlLabel value="comma" control={<Burger.RadioButton />} label={intl.formatMessage({id: 'debug.csv.download.delimiter.comma'})} />
        <FormControlLabel value="semicolon" control={<Burger.RadioButton />} label={intl.formatMessage({id: 'debug.csv.download.delimiter.semicolon'})} />
      </RadioGroup>
      <p>{intl.formatMessage({id: 'debug.csv.download.options'})}</p>
      <Burger.Checkbox checked={wrap} onChange={() => setWrap(!wrap)} />
      <label>{intl.formatMessage({ id: 'debug.csv.download.wrap' })}</label>
    </>
  );


  return (<Box sx={{ width: '100%', overflow: 'hidden', padding: 1 }}>

    <DebugDrawer selected={selected} open={option === "DRAWER"} onClose={() => setOption(undefined)} onSelect={setOption} />

    {option === 'SELECT_ASSET' ? <SelectAsset onClose={() => setOption(undefined)} selected={selected} onSelect={handleSelectAsset} /> : null}
    {option === 'INPUT_JSON' && json ? <InputJSON onClose={() => setOption(undefined)} onSelect={handleJson} value={json} /> : null}
    {option === 'INPUT_FORM' && json ? <InputFORM onClose={() => setOption(undefined)} selected={selected} onSelect={handleJson} value={json} /> : null}
    {option === 'INPUT_CSV' ? <InputCSV onClose={() => setOption(undefined)} selected={selected} onSelect={handleCsv} value={csv} /> : null}


    <TableContainer sx={{ height: "calc(100vh - 150px)" }}>
      <Table stickyHeader size="small">
        <DebugHeader>
          {ast ?
            (<Burger.PrimaryButton label={`${ast?.bodyType} - ${ast?.name}`} onClick={() => setOption('SELECT_ASSET')} />) :
            (<Burger.PrimaryButton label="debug.toolbar.noAsset" onClick={() => setOption('SELECT_ASSET')} />)
          }
          <Burger.PrimaryButton disabled={selected ? false : true} label="debug.toolbar.openAsset" onClick={() => entity && nav.handleInTab({ article: entity })} sx={{ ml: 1 }} />
          <Burger.PrimaryButton label="debug.toolbar.options" onClick={() => setOption('DRAWER')} sx={{ ml: 1 }} />
          <Burger.PrimaryButton disabled={selected ? false : true} label="debug.toolbar.execute" onClick={() => handleExecute()} sx={{ ml: 1 }} />
          {inputType === 'CSV' && debug?.debug?.bodyCsv ? <Burger.PrimaryButton disabled={selected ? false : true} label="debug.toolbar.download" onClick={() => setDialogShow(true)} sx={{ ml: 1 }} /> : null}
        </DebugHeader>

        <TableBody>
          {json ? <DebugInput type={inputType} csv={csv} json={json} /> : null}
          {error ? <DebugError error={error} /> : null}
          {response ? <DebugOutput debug={response} selected={ast} /> : null}
        </TableBody>
      </Table>
    </TableContainer>

    { dialogShow && <Burger.Dialog open={true}
      onClose={() => setDialogShow(false)}
      backgroundColor="uiElements.main"
      children={dialogChildren}
      title='debug.csv.download'
      submit={{
        title: "buttons.download",
        disabled: false,
        onClick: () => downloadCsv(delimiter, wrap)
      }}
    /> }

  </Box >);
}

export { DebugView };
