import * as monaco_editor from 'monaco-editor';

import { HdesApi } from '@dxs-ts/wrench-api';
import { CompletionDialogProps } from '../autocomplete';




export const toLowerCamelCase = (value: string) => {
  if (value) {
    return value.replace(/^([A-Z])|\s(\w)/g, function(_match, p1, p2, _offset) {
      if (p2) return p2.toUpperCase();
      return p1.toLowerCase();
    });
  }
}

export const executeTemplate = (cm: typeof monaco_editor, value: any, guided: CompletionDialogProps, asset?: HdesApi.AstBody) => {
  const [model] = cm.editor.getModels();
  const content = model.getLineContent(guided.position.lineNumber);

  const lines: string[] = [];
  if (guided.append) {
    lines.push(content);
  } else {
    lines.push('');
  }
  lines.push(...parseTemplate(value, [guided.value]));

  if (asset) {
    const params = asset.headers.acceptDefs.map(p => '          ' + p.name + ':');
    lines.push(...params)
  }
  model.applyEdits([{
    range: guided.range,
    text: lines.join('\r\n') + '\r\n'
  }]);
}


const parseTemplate = (toBeReplaced: any, template: string[]): string[] => {
  const result: string[] = [];
  for (let v of template) {
    let line: string = v;

    for (let key of Object.keys(toBeReplaced)) {
      const replacable = '{' + key + '}';
      if (line.indexOf(replacable) < 0) {
        continue;
      }
      if (toBeReplaced[key] === undefined) {
        return result;
      }
      line = line.replace(replacable, toBeReplaced[key])
    }
    result.push(line)
  }

  return result
}

