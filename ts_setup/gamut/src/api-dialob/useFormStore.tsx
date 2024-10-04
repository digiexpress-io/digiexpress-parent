import React from 'react'
import { ActionsQueue, ActionsQueueSyncEvent, EventHandler } from './impl-actions-queue'
import { FormImpl } from './impl-form'

import { DialobApi } from './dialob-types'
import { ActionVisitor } from './impl-actions-visitor'
import { useDialob } from './DialobContext'


export function useFormStore(props: {id: string}): DialobApi.FormStore {
  const { id } = props;
  const { fetchGet, fetchPost, syncWait } = useDialob();
  const [pending, setPending] = React.useState(true);
  const [form, setState] = React.useState(new FormImpl(id, new ActionVisitor().withActions([])));

  const syncListener: EventHandler<ActionsQueueSyncEvent> = React.useCallback(({syncState, response}) => {
    if (syncState === 'DONE' && response?.actions) {
      setState(prev => prev.withState(new ActionVisitor(prev.state).withActions(response.actions as DialobApi.Action[])))
    }
  }, []);

  const queue = React.useMemo(
    () => new ActionsQueue({id, syncWait: syncWait || 250, error: [], fetchGet, fetchPost, sync: [syncListener]}), 
    [id, syncWait, syncListener, fetchGet, fetchPost]);

  React.useEffect(() => {
    queue.pull().then(() => setPending(false));
  }, [queue]);

  const applyActions: (actions: DialobApi.Action[]) => void = React.useCallback((actions) => {
    setState(prev => prev.withState(new ActionVisitor(prev.state).withActions(actions)));
  }, []);

  const queueAction: (actions: DialobApi.Action) => void = React.useCallback((action) => {
    applyActions([action]);
    queue!.add(action);
  }, [queue, applyActions]);

  const addRowToGroup: (rowGroupId: string) => void = React.useCallback((rowGroupId) => {
    queueAction({ type: 'ADD_ROW', id: rowGroupId })
  }, [queueAction]);

  const deleteRow: (rowId: string) => void = React.useCallback((rowId) => {
    queueAction({ type: 'DELETE_ROW', id: rowId });
  }, [queueAction]);

  const complete: () => void = React.useCallback(() => {
    queueAction({ type: 'COMPLETE' });
  }, [queueAction]);

  const next: () => void = React.useCallback(() => {
    queueAction({ type: 'NEXT' });
  }, [queueAction]);

  const previous: () => void = React.useCallback(() => {
    queueAction({ type: 'PREVIOUS' });
  }, [queueAction]);

  const goToPage: (pageId: string) => void = React.useCallback((pageId) => {
    queueAction({ type: 'GOTO', id: pageId });
  }, [queueAction]);

  const setLocale: (locale: string) => void = React.useCallback((locale) => {
    setState(prev => {
      if (locale !== prev.state.locale) {
        queueAction({ type: 'SET_LOCALE', value: locale });
      }
      return prev;
    });
  }, [queueAction]);

  const setAnswer: (itemId: string, answer: any) => void = React.useCallback((itemId, answer) => {
    queueAction({ type: 'ANSWER', answer, id: itemId, });
  }, [queueAction]);

  const pull: () => Promise<void> = React.useCallback(() => queue.pull(), [queue]);


  return React.useMemo(() => Object.freeze({
    setAnswer, setLocale, goToPage, previous, next, complete, deleteRow, addRowToGroup, pull,
    id: queue.id,
    completed: form.state.completed ?? false,
    locale: form.state.locale,
    form, pending,
  }), [setAnswer, setLocale, goToPage, previous, next, complete, deleteRow, addRowToGroup, form, queue, pending]);
}
