import { DialobApi } from './dialob-types';


export function parseInputRow(inputRowId: string, session: DialobApi.Form): DialobApi.ControlInputRow {
  const inputRow = session.getItem(inputRowId);
  const [groupId] = session.state.reverseItemMap[inputRowId];
  const inputGroup = session.getItem(groupId);

  return {
    id: inputRowId,
    source: inputRow!,
    order: inputGroup?.items?.indexOf(inputRowId) ?? 0,
    total: inputGroup?.items?.length ?? 0
  };
}
