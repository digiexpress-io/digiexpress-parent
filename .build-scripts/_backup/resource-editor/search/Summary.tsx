import React from 'react';
import { CircularProgress } from '@material-ui/core';


import Resource from '../';


interface SummaryProps {
  id: string;
  
};

const Summary: React.FC<SummaryProps> = ({id}) => {
  
  const resource = Resource.useContext();
  const content = resource.session.editor.getContent(id);  
  const model = resource.session.getModel(id);
  const [loaded, setLoaded] = React.useState<string>();

  // nothing active
  React.useEffect(() => {
    if(loaded !== id) {
      resource.actions.getAsset(id)
        .then(data => {
          setLoaded(id);
        })
        .catch(e => {
          console.error('failed to load', e);
          setLoaded(id);
        });  
    }

  }, [resource.actions, loaded, setLoaded, id]);
  
  if(!content) {
    return <CircularProgress /> 
  }
  
  
  if(model.type === "FLOW" || model.type === "FLOW_TASK" || model.type === "DT") {
    return (<Resource.EditType id={id} />);
  }
  
  return (<></>)
}
    
export default Summary;