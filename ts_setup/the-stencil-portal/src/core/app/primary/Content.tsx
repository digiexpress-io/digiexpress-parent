import React from 'react';

//import { useBlob} from '../';
import { Markdown } from './Markdown';
import Portal from '../../';


interface AppContentProps {

}

const Content: React.FC<AppContentProps> = () => {
  const view = Portal.useTopic();

  if (view && view.blob) {
    return (<Markdown children={view.blob.value} />);
  }
  
  return null;

}


export { Content }

