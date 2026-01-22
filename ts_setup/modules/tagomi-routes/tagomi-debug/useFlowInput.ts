import React from 'react';
import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';

export function useFlowInput(orchestratorName: HdesApi.EntityId) {
  const { flows } = Composer.useSite();
  const [input, setInput] = React.useState<Record<string, any>>({});
  
  const asset: HdesApi.Entity<HdesApi.AstBody> | undefined =
    React.useMemo(() => {
      if (flows[orchestratorName]) {
        return flows[orchestratorName];
      }
      return Object.values(flows).find(
        (flow) => flow.ast?.name === orchestratorName
      );
    }, [orchestratorName, flows]);

  const elements = React.useMemo(() => {
    return asset?.ast?.headers?.acceptDefs ?? [];
  }, [asset]);

  React.useEffect(() => {
    const initialJson: Record<string, any> = {};
    elements.forEach((def) => {
      if (def.values) {
        initialJson[def.name] = def.values;
      }
    });

    if (Object.keys(initialJson).length > 0) {
      setInput(initialJson);
    }
  }, [elements]);

  return {
    asset,
    elements,
    input,
    setInput,
  };
}
