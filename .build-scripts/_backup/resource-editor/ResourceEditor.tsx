import React from 'react';

import { Hdes, Layout } from './deps';
import Resource from './';


const sessionData = new Layout.SessionData({
  dimensions: { width: 0, height: 0 },
  drawer: { open: false, width: 300 }
});


interface ResourceEditorProps {
  store: Hdes.Store;
  theme: 'light' | 'dark';
};


const Components: React.FC = () => {
  const resource = Resource.useContext();
  return (<Layout.Components children={{
    header: () => ({ page: <></> }),
    badges: () => [],
    drawer: () => ({}),
    content: () => Resource.createContent(),
    search: (layout) => Resource.createSearch(layout),
    toolbar: (layout) => Resource.createToolbar({ resource, layout })
  }} />);
}

const ResourceEditor: React.FC<ResourceEditorProps> = ({ store, theme }) => {
  const [session, dispatch] = React.useReducer(Layout.Reducer, sessionData);
  const actions = React.useMemo(() => new Layout.ReducerDispatch(dispatch), [dispatch]);
  const backend = React.useMemo(() => new Hdes.ServiceImpl(store), [store]);

  return (
    <Layout.Context.Provider value={{ session, actions }}>
      <Resource.Provider service={backend} theme={theme}>
        <Resource.Debug.Provider>
          <Components />
        </Resource.Debug.Provider>
      </Resource.Provider>
    </Layout.Context.Provider>


  );
}

export type { ResourceEditorProps };
export { ResourceEditor };
