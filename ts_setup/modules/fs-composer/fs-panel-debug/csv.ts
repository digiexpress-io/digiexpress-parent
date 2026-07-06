
export const downloadCsv = (delimiter: string, wrap: boolean, bodyCsv: string | undefined) => {
  let content: string = bodyCsv ? bodyCsv : "";
  
  if (content.includes('\r')) {
    content = content.replace(/\r/g, '');
  }
  if (content.endsWith("\n")) {
    content = content.substring(0, content.length - 1);
  }

  const lines = content.split('\n');
  const outputHeaders = lines[0].split(',');
  const outputLines = lines.slice(1, lines.length / 2);
  const inputHeaders = lines[lines.length / 2].split(';');
  const inputLines = lines.slice(lines.length / 2 + 1, lines.length);
  let finalContent = "";

  if (wrap) {
    finalContent = delimiter === "comma" ?
      wrapAndComma(outputHeaders, outputLines, inputHeaders, inputLines) :
      wrapAndSemicolon(outputHeaders, outputLines, inputHeaders, inputLines);
  } else {
    finalContent = delimiter === "comma" ?
      formatCsvForDownloadWithComma(outputHeaders, outputLines, inputHeaders, inputLines) :
      formatCsvForDownloadWithSemicolon(outputHeaders, outputLines, inputHeaders, inputLines);
  }

  const blob = new Blob([finalContent], { type: 'text/csv' });
  const url = window.URL.createObjectURL(blob);
  const pom = document.createElement('a');
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
    return inputLine.replace(';', ',').concat(',').concat(outputLine);
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
      if (outputValue.startsWith('"')) {
        for (let k = j + 1; k < outputValues.length; k++) {
          const outputValue2 = outputValues[k];
          if (outputValue2.endsWith('"')) {
            outputValues[j] = outputValues.slice(j, k + 1).join(',').slice(1, -1);
            outputValues.splice(j + 1, k - j);
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
      if (outputValue.startsWith('"') && !outputValue.includes(';')) {
        for (let k = j + 1; k < outputValues.length; k++) {
          const outputValue2 = outputValues[k];
          if (outputValue2.endsWith('"')) {
            outputValues[j] = outputValues.slice(j, k + 1).join(',').slice(1, -1);
            outputValues.splice(j + 1, k - j);
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
