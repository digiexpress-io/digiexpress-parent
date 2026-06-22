import React from 'react';
import { Fs, useFsDirent, useFsuValue } from '@dxs-ts/fs-api';
import { FsPanelPreviewWrenchProps } from './FsPanelPreviewWrenchProps';


export interface OwnerState {
  flowAst: Fs.FlowAst | undefined;
  wrenchBody: Fs.WrenchBody | undefined;
}

export const useOwnerState = (props: FsPanelPreviewWrenchProps): OwnerState => {
  const { fetchDirentBody, applyTransientChanges } = useFsDirent();
  const { state } = useFsuValue(props.dirent.id);
  const [wrenchBody, setWrenchBody] = React.useState<Fs.WrenchBody | undefined>(undefined);
  const [flowAst, setFlowAst] = React.useState<Fs.FlowAst>();

  // Hook up our debounced string (waits 500ms after typing stops)
  const fromEdit: string | undefined = useDebounce(state?.getCurrentProps().changes['flowValue'] ?? wrenchBody?.flows[props.dirent.id]?.ast.parseTree.value);

  React.useEffect(() => {
    fetchDirentBody(props.dirent.id, 'FLOW')
      .then(body => setWrenchBody(body as Fs.WrenchBody));
  }, [props.dirent.commitIndex?.treeId]);

  React.useEffect(function loadAst() {
    if (!fromEdit) {
      return;
    }
    applyTransientChanges({
      id: props.dirent.id,
      bodyType: 'FLOW',
      bodyStatment: [],
      bodySyntax: fromEdit,
    }).then((body) => {
      const wb = body as Fs.WrenchAstBody<Fs.FlowAst>;
      setFlowAst(wb.ast);
    });

  }, [props.dirent.id, fromEdit]);

  return {
    wrenchBody,
    flowAst: flowAst ?? wrenchBody?.flows[props.dirent.id]?.ast,
  };
};



const delay = 1000;
function useDebounce(value: string) {
  const [debouncedValue, setDebouncedValue] = React.useState(value);

  React.useEffect(() => {
    // Set a timer to update the debounced value after the delay
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]); 

  return debouncedValue;
}