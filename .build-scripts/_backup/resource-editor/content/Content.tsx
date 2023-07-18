import React from 'react';
import { FormattedMessage } from 'react-intl';

import { Layout } from '../deps';
import Resource from '../';


interface ContentProps {
};


const reserved: string[] = ['search', 'releases', 'debug'];

const Content: React.FC<ContentProps> = () => {
  const layout = Layout.useContext();
  const resource = Resource.useContext();
  const [loaded, setLoaded] = React.useState<string>();
  
  // nothing active
  const tabs = layout.session.tabs;
  const active = tabs[layout.session.history.open];
    
  React.useEffect(() => {
    if(active && !loaded && !reserved.includes(active.id)) {
      const content = resource.session.editor.getContent(active.id);
      if(!content) {
        resource.actions
          .getAsset(active.id)
          .catch(_e => setLoaded(active.id));  
      }
    }

  }, [resource, active, loaded, setLoaded]);

  if(tabs.length === 0 || !active ) {
    return null;
  }
  
  if(active.id === 'search') {
    return <Resource.Search children={layout.session.search}/>; 
  }
  if(active.id === 'releases') {
    return <Resource.Releases />; 
  }
  if(active.id === 'debug') {
    return <Resource.Debug.Explorer />; 
  }

  const editor = resource.session.editor.getContent(active.id);
  if(!editor) {
    return <FormattedMessage id="content.loading" />
  }
  
  const model = editor.origin;
  if(model.type === "FLOW" || model.type === "FLOW_TASK" || model.type === "DT") {
    return (<Resource.EditType id={active.id} />);
  }
  return (<div>{editor.content}</div>);
}

const createContent = (): Layout.Session.Content => ({page: <Content />});
export type {ContentProps}
export {Content, createContent};
