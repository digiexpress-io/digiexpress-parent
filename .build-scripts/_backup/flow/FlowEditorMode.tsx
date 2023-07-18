import React from 'react';
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Grid } from '@material-ui/core';

import { CodeEditor, Hdes } from './deps';
import { createHints, HelperContext, Autocomplete } from './autocomplete';
import FlowTree from './graph';


const useStyles = (size: { width: number, height: number }) => makeStyles((_theme: Theme) =>
  createStyles({
    root: {
      //width: "100%",
      //height: "100%",
      flexGrow: 1,
    },
    editor: {
      height: size.height,
      //overflow: 'scroll'
    },
    graph: {
      height: size.height,
      //overflow: 'scroll'
    }
  }),
)();

interface FlowEditorModeProps {
  models: Hdes.ModelAPI.Models;
  theme: {
    mode: 'dark' | 'light'
  }
  size: {
    width: number, height: number
  };
  children: Hdes.AstAPI.FlCommand[];
  ast: (commands: Hdes.AstAPI.FlCommand[], rev?: number) => Promise<Hdes.AstAPI.Fl>;
  onChanges: (changes: Hdes.AstAPI.FlCommand[]) => void;
  onCreate: (props: { name: string, type: Hdes.ModelAPI.ServiceType }) => Promise<void>;
  onOpen: (assetId: string) => void;
};


const FlowEditorMode: React.FC<FlowEditorModeProps> = (props) => {
  const classes = useStyles(props.size);
  const [parser, setParser] = React.useState<Hdes.AstAPI.Fl>();
  const [autocomplete, setAutocomplete] = React.useState<HelperContext>();

  React.useEffect(() => {
    props.ast(props.children).then(setParser).catch(e => { console.error(e); })
  }, [props])

  
  if (!parser) {
    return null;
  }

  return (<div className={classes.root}>
    <Autocomplete value={autocomplete} onClose={() => setAutocomplete(undefined)} fl={parser} models={props.models} onCreate={props.onCreate}/>
    <Grid container spacing={0}>
      <Grid item xs={6} className={classes.editor}>
        <CodeEditor.Provider mode="fl" theme={props.theme.mode} 
          onCommands={props.onChanges}
          lint={() => parser.messages}
          hint={(pos, content) => createHints({pos, content, fl: parser, helper: (context) => setAutocomplete(context)})}>
          
          {parser.src.value}
        </CodeEditor.Provider>
      </Grid>
      <Grid item xs={6} className={classes.graph}>
        <FlowTree.Provider
          models={props.models}
          onClick={(data) => {}}
          onDoubleClick={(data) => props.onOpen(data)}>
          {parser}
        </FlowTree.Provider>
      </Grid>
    </Grid>
  </div>);
}

export type { FlowEditorModeProps }
export { FlowEditorMode };
