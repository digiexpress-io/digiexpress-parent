import React from 'react';
import Context from 'context';
import { DialobList } from './DialobListTabState';
import DialobItemActive from './DialobItemActive';
import { DialobItem } from './DialobItem';



const DialobListLoader: React.FC = () => {
  const entries = Context.useTenants();
  if (entries.loading) {
    return <>...loading</>
  }

  return (<DialobList>{{ DialobItem, DialobItemActive }}</DialobList>);
}

export default DialobListLoader;