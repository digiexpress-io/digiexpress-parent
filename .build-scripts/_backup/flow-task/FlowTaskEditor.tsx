import React from 'react';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Grid } from '@material-ui/core';

import { CodeEditor, Hdes } from './deps';


const useStyles = (size: { width: number, height: number }) => makeStyles((_theme: Theme) =>
  createStyles({
    root: {
      flexGrow: 1,
    },
    editor: {
      height: size.height,
    },
  }),
)();

interface FlowTaskEditorProps {
  models: Hdes.ModelAPI.Models;
  theme: {
    mode: 'dark' | 'light'
  }
  size: {
    width: number, height: number
  };
  children: Hdes.AstAPI.FtCommand[];
  ast: (commands: Hdes.AstAPI.FtCommand[], rev?: number) => Promise<Hdes.AstAPI.Ft>;
  onChanges: (changes: Hdes.AstAPI.FtCommand[]) => void;
};


const FlowTaskEditor: React.FC<FlowTaskEditorProps> = (props) => {
  const classes = useStyles(props.size);
  const [parser, setParser] = React.useState<Hdes.AstAPI.Ft>();

  React.useEffect(() => {
    props.ast(props.children).then(setParser).catch(e => { console.error(e); })
  }, [props])

  if (!parser) {
    return null;
  }

  return (<div className={classes.root}>
    <Grid container spacing={0}>
      <Grid item xs={12} className={classes.editor}>
        <CodeEditor.Provider mode="ft" theme={props.theme.mode} onCommands={props.onChanges}>
          {parser?.src}
        </CodeEditor.Provider>
      </Grid>
    </Grid>
  </div>);
}

export type { FlowTaskEditorProps }
export { FlowTaskEditor };
