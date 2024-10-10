import ReactDOM from 'react-dom';
import 'codemirror/addon/lint/lint';
import 'codemirror/addon/hint/show-hint';
import 'codemirror/addon/scroll/simplescrollbars';
import 'codemirror/mode/groovy/groovy'; // eslint-disable-line
import 'codemirror/mode/yaml/yaml'; // eslint-disable-line

import 'codemirror/theme/eclipse.css';
import 'codemirror/lib/codemirror.css';
import 'codemirror/addon/lint/lint.css';
import 'codemirror/addon/hint/show-hint.css';
import 'codemirror/addon/scroll/simplescrollbars.css';

import NewApp from './application/NewApp';

ReactDOM.render(
  NewApp,
  document.getElementById('root')
);

