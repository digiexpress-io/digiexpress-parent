import React from 'react';

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

import { styled } from "@mui/material/styles";
import { Box, BoxProps } from '@mui/material';

import { View, ViewProps, createView } from './ViewImpl';


const StyledBox = styled(Box)<BoxProps>(({ theme }) => ({
  width: "100%",
  height: "100%",
  "& .CodeMirror": {
    width: "100% !important",
    height: "100% !important",
    fontSize: "13px",
    lineHeight: "1.5",
  }
}));

const CodeEditorState: React.FC<ViewProps> = (props) => {
  const ref = React.useRef<HTMLTextAreaElement>(null);
  const [state, setState] = React.useState<View>();
  
  React.useEffect(() => {
    setState(createView(ref, props).withEvents(props));

    return () => {
      try {
        state?.remove();
      } catch(e) {}
    }
  }, [ref.current]);
  return (<textarea key={props.id} id={props.id} ref={ref} />);
}

const CodeEditor: React.FC<ViewProps> = (props) => {
  const [state, setState] = React.useState<string>(props.id);
  const [loading, setLoading] = React.useState<boolean>(false);
  
  React.useEffect(() => {

    if(props.id !== state) {
      setLoading(true)
      setState(props.id);
    }
  }, [props.id]);

  React.useEffect(() => {
    if(loading) {
      setLoading(false);
    }
  }, [loading]);

  if(loading) {
    return (<></>)
  }
  return (<StyledBox><CodeEditorState {...props} /></StyledBox>);
}
export { CodeEditor };
