import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  wrenchBody: Fs.WrenchBody | undefined;
  decision: Fs.DecisionAst | undefined;
  onChangeCommands: (commands: Fs.AstCommand[]) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, fetchDirentBody, applyTransientChanges } = useFsDirent();

  const dirent = getDirent(props.direntId);

  const [wrenchBody, setWrenchBody] = React.useState<Fs.WrenchBody | undefined>(undefined);
  const [decision, setDecision] = React.useState<Fs.DecisionAst | undefined>(undefined);
  const [commands, setCommands] = React.useState<Fs.AstCommand[]>([]);

  React.useEffect(() => {
    fetchDirentBody(props.direntId, 'DECISION_TABLE').then((body) => {
      const wb = body as Fs.WrenchBody;
      setWrenchBody(wb);
      setDecision(wb.decisions[props.direntId]?.ast);
      setCommands(wb.decisions[props.direntId]?.commands ?? []);
    });
  }, [props.direntId]);

  const onChangeCommands = React.useCallback((newCommands: Fs.AstCommand[]) => {
    const allCommands = [...commands, ...newCommands];
    setCommands(allCommands);

    applyTransientChanges({
      id: props.direntId,
      bodyType: 'DECISION_TABLE',
      bodyStatment: allCommands,
    }).then((body) => {
      const wb = body as Fs.WrenchAstBody<Fs.DecisionAst>;
      setDecision(wb.ast);
    });
    console.log("allCommands", allCommands)
  }, [commands, props.direntId, applyTransientChanges]);


  return ({
    isDarkMode,
    dirent,
    wrenchBody,
    decision,
    onChangeCommands,
  });
};
