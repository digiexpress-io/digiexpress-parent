import React from 'react';
import { DebugOutputCsvRow } from './DebugOutputCsvRow';
import { Client } from '../../context';

const mapCsvRows = (debug: string): Client.CsvRow[] => {
    if (!debug) {
        return [];
    }
    const result: Client.CsvRow[] = [];
    // remove carriage returns
    if (debug.includes('\r')) {
        debug = debug.replace(/\r/g, '');
    }
    // remove empty row at the end (occurs with file import)
    if (debug.endsWith("\n")) {
        debug = debug.substring(0, debug.length - 1);
    }
    const lines = debug.split('\n');
    const outputHeaders = lines[0].split(',');
    const outputLines = lines.slice(1, lines.length/2);
    const inputHeaders = lines[lines.length/2].split(';');
    const inputLines = lines.slice(lines.length/2+1, lines.length);
    for (let i = 0; i < inputLines.length; i++) {
        const inputLine = inputLines[i];
        const outputLine = outputLines[i];
        const inputValues = inputLine.split(';');
        const outputValues = outputLine.split(',');
        // handling cases where output contains a comma
        for (let j = 0; j < outputValues.length; j++) {
            const outputValue = outputValues[j];
            if (outputValue.startsWith('"') && !outputValue.includes(';')) {
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
        // handling cases where output contains a semicolon
        for (let j = 0; j < outputValues.length; j++) {
            const outputValue = outputValues[j];
            if (outputValue.startsWith('"') && outputValue.includes(';')) {
                outputValues[j] = outputValue.slice(1, -1);
            }
        }
        const rowId = outputValues[0];
        const csvRow: Client.CsvRow = { id: rowId, inputs: [], outputs: [] };
        for (let j = 0; j < inputValues.length; j++) {
            csvRow.inputs.push({
                name: inputHeaders[j],
                value: inputValues[j]
            });
        }
        for (let j = 1; j < outputValues.length; j++) {
            if (outputValues[j] !== '') {
                csvRow.outputs.push({
                    name: outputHeaders[j],
                    value: outputValues[j]
                });
            }
        }
        result.push(csvRow);
    }
    return result;
}

const DebugOutputCsv: React.FC<{ debug: string }> = ({ debug }) => {
    const csvRows = mapCsvRows(debug);
    return (<>
      {csvRows.map((csvRow: Client.CsvRow) => <DebugOutputCsvRow key={csvRow.id} csvRow={csvRow} index={csvRow.id}/>)}
    </>);
}
  
export { DebugOutputCsv };