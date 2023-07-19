import CodeMirrorIntegration from 'codemirror';
import 'codemirror/addon/mode/simple';
import {RESERVED_WORDS} from './language';

CodeMirrorIntegration.defineSimpleMode('del', {
    start: [
      {regex: /'(?:[^\\]|\\.)*?(?:'|$)/, token: 'string'},
      {regex: /"(?:[^\\]|\\.)*?(?:"|$)/, token: 'string'},
      {regex: /0x[a-f\d]+|[-+]?(?:\.\d+|\d+\.?\d*)(?:e[-+]?\d+)?/i, token: 'number'},
      {regex: new RegExp(`(?:${RESERVED_WORDS.join('|')})\\b`), token: 'keyword'},
      {regex: /[-+\/*=<>!]+/, token: 'operator'},
      {regex: /[a-z$][\w$]*/, token: 'variable'}
    ]
  }
);
