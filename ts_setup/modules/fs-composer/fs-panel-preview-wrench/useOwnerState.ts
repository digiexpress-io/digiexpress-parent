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

  const bodySyntax = useDebounce(state?.getCurrentProps().changes['flowValue'] ?? wrenchBody?.flows[props.dirent.id]?.ast.parseTree.value);



  React.useEffect(() => {
    fetchDirentBody(props.dirent.id, 'FLOW')
      .then(body => setWrenchBody(body as Fs.WrenchBody));
  }, [props.dirent.commitIndex?.treeId]);

  React.useEffect(function loadAst() {
    if (!bodySyntax) {
      return;
    }
    applyTransientChanges({
      id: props.dirent.id,
      bodyType: 'FLOW',
      bodyStatment: [],
      bodySyntax: bodySyntax,
    }).then((body) => {
      const wb = body as Fs.WrenchAstBody<Fs.FlowAst>;
      setFlowAst(wb.ast);
    });

  }, [bodySyntax]);

  return {
    wrenchBody,
    flowAst: flowAst ?? wrenchBody?.flows[props.dirent.id]?.ast,
  };
}

const delay = 1000;
function useDebounce(value: string | undefined) {
  const [debouncedValue, setDebouncedValue] = React.useState(value);

  React.useEffect(() => {
    const handler = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(handler);
  }, [value]);

  return debouncedValue;
}