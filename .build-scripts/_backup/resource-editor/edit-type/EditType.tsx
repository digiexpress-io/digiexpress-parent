import React from 'react';
import { FormattedMessage } from 'react-intl';
import { Alert, Button, useTheme } from '@material-ui/core';
import CloseIcon from '@material-ui/icons/Close';

import { Layout, Hdes, DecisionTable, FlowTask, Flow } from '../deps';
import { ErrorList } from '../errors';
import {openTab} from '../explorer/Tab';
import Resource from '../';


interface EditTypeProps {
  id: string
};

type Commands = Hdes.AstAPI.DtCommand[] | Hdes.AstAPI.FlCommand[] | Hdes.AstAPI.FtCommand[];

const EditType: React.FC<EditTypeProps> = ({ id }) => {
  const theme = useTheme();
  const layout = Layout.useContext();
  const resource = Resource.useContext();
  const [loaded, setLoaded] = React.useState<string>();
  const [displayErrors, setDisplayErrors] = React.useState(true)
  const actions = resource.actions;

  React.useEffect(() => {
    if (!loaded) {
      const content = resource.session.editor.getContent(id);
      if (!content) {
        resource.actions.getAsset(id).catch(_e => setLoaded(id));
      }
    }

  }, [resource, id, loaded, setLoaded]);

  const onChange = React.useCallback((changes: Commands) => {
    actions.setEditor({ id, content: changes });
  }, [id, actions]);


  const editor = resource.session.editor.getContent(id);
  if (!editor) {
    return <FormattedMessage id="content.loading" />
  }
  
  const serviceType = editor.origin.type;

  const ast = (commands: Commands, rev?: number) => resource.service.ast.getParser({
      rev: rev ? rev : commands.length,
      type: serviceType,
      input: commands
    }).then(data => data.service as any);

  const origin = editor.start;
  const session = editor.content;
  const children = [...origin, ...(session ? session : []) as any];
  let edit: React.ReactElement;
  if (serviceType === "FLOW") {
    return (<Flow.Provider key={id} theme={{ mode: theme.palette.mode === 'dark' ? 'dark' : 'light' }}
      size={layout.session.dimensions} models={resource.session.models}
      ast={ast} onChanges={onChange} children={children}
      onOpen={(id) => {
        for(const models of Object.values(resource.session.models)) {
          for(const model of models) {
            if(model.id === id) {
              openTab(model, layout.actions);
              break;  
            }
            
          }
        }
        
      }}
      onCreate={(props) => resource.actions.createAsset({ name: props.name, serviceType: props.type }).then()} />);
  } else if (serviceType === "FLOW_TASK") {
    edit = (<FlowTask.Provider key={id} theme={{ mode: theme.palette.mode === 'dark' ? 'dark' : 'light' }} 
      size={layout.session.dimensions} models={resource.session.models}
      ast={ast} onChanges={onChange} children={children} />);
  } else if (serviceType === "DT") {
    edit = (<DecisionTable.Provider key={id} 
      size={layout.session.dimensions}  models={resource.session.models}
      ast={ast} onChange={onChange} children={children} />);
  } else {
    edit = (<></>);
  }

  return (<div>
    {editor.origin.errors.length > 0 && displayErrors ? (
      <Alert severity="error" action={<Button onClick={() => setDisplayErrors(false)}><CloseIcon /></Button>}>
        <ErrorList>
          {new Hdes.StoreError({ text: "", status: 0, errors: editor.origin.errors })}
        </ErrorList>
      </Alert>
    ) : null}
    {edit}
  </div>);
}

export type { EditTypeProps }
export { EditType };
