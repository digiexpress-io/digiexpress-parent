import React from 'react';
import { makeStyles, createStyles } from '@material-ui/styles';

import 'codemirror/addon/lint/lint';
import 'codemirror/addon/hint/show-hint';
import 'codemirror/addon/scroll/simplescrollbars';
import 'codemirror/mode/groovy/groovy';
import 'codemirror/mode/yaml/yaml';

import 'codemirror/theme/monokai.css';
import 'codemirror/theme/eclipse.css';
import 'codemirror/lib/codemirror.css';
import 'codemirror/addon/lint/lint.css';
import 'codemirror/addon/hint/show-hint.css';
import 'codemirror/addon/scroll/simplescrollbars.css';

import Editor from './context/Context';


const useStyles = makeStyles(() =>
  createStyles({
    root: {
      width: "100%",
      height: "100%",
      "& .CodeMirror": {
        width: "100% !important",
        height: "100% !important",
        fontSize: "13px",
        lineHeight: "1.5",
      }
    },
  }),
);


const Integration: React.FC<{}> = () => {
  const classes = useStyles();
  const ref = React.createRef<HTMLTextAreaElement>();
  const context = Editor.useContext();
  
  React.useLayoutEffect(() => {
    if(!ref.current) {
      return;
    }
    if(context.session.view) {
      return; 
    }
    context.actions.setEditor(ref);
  }, [ref, context])

  return (<div className={classes.root}><textarea ref={ref} /></div>);
}
export default Integration;
