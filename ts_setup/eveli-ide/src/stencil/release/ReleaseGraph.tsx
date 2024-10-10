import React from 'react';
import { Box } from '@mui/material';


import { Composer, StencilClient } from '../context';
import { ReleaseGraphVisitor } from './ReleaseGraphVisitor';



const ReleaseGraphRef: React.FC<{ site: StencilClient.Site }> = ({ site }) => {
  const ref = React.createRef<HTMLDivElement>();

  React.useLayoutEffect(() => {
    new ReleaseGraphVisitor({ site, ref }).visit();
  }, [ref, site])

  return (<Box display="flex" width="900px" height="900px" ref={ref} />);
}

const ReleaseGraph: React.FC<{}> = () => {
  const { site } = Composer.useComposer();

  //const graph = React.useMemo(() => <ReleaseGraphRef site={site} />, [site])
  //return (graph);
  return  <ReleaseGraphRef site={site} />;
 
}


export { ReleaseGraph }




