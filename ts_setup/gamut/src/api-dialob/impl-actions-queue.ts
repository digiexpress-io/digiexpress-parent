import { DialobApi } from './dialob-types';

export interface ActionsQueueOptions {
  id: string; // Form session id
  syncWait: number;
  sync: EventHandler<ActionsQueueSyncEvent>[];
  error: EventHandler<ActionsQueueErrorEvent>[];
  fetchActionPost: DialobApi.FetchActionPOST;
  fetchActionGet: DialobApi.FetchActionGET;
}

export interface ActionsQueueResponse {
  rev: number;
  actions?: DialobApi.Action[];
}
export type ActionsQueueSyncEvent = {
  syncState: 'INPROGRESS' | 'DONE';
  response?: ActionsQueueResponse;
}
export type ActionsQueueErrorEvent = {
  type: 'SYNC' | 'SYNC-REPEATED';
  error: ActionsQueueError;
}
export type EventHandler<EventType> = (event: EventType) => void

export class ActionsQueue {
  private options: ActionsQueueOptions;
  private inSync = false;
  private retryCount = 0;
  private rev = 0;
  private syncTimer?: ReturnType<typeof setTimeout>;
  private syncActionQueue: DialobApi.Action[] = [];
  private syncQueueImmediately = false;

  constructor(options: ActionsQueueOptions) {
    if (options.syncWait < -1) {
      throw new Error('syncWait must be -1 or higher!');
    }
    this.options = options;
  }

  public get id() {
    return this.options.id;
  }

  public async pull(): Promise<void> {
    try {
      await this.runSyncFn(async () => {
        const response = await this.options.fetchActionGet(this.id);
        if (!response.ok) {
          throw new ActionsQueueRequestError('Failure during fetch', response.status);
        }
        const json = await response.json();

        console.log(json);
        return json;
      });
    } catch (e: any) {
      this.handleError(e);
    }
  }

  public add(action: DialobApi.Action) {
    if (this.options.syncWait === -1) {
      // We use queue here instead of calling this.sync() directly, because if sync fails we need
      // to re-try and to have an efficient re-try, we need to work with an action queue anyway.
      // Better to re-use the existing logic than to have multiple implementations.
      this.addToSyncQueue(action);
      this.syncQueuedActions();
    } else {
      this.clearDeferredSync();
      this.addToSyncQueue(action);
      if (this.syncQueueImmediately || syncActionImmediately(action)) {
        this.syncQueueImmediately = true;
        this.syncQueuedActions();
      } else {
        this.deferSync();
      }
    }
  }

  private addToSyncQueue(action: DialobApi.Action): void {
    let add = false;
    if (action.type === 'ANSWER') {
      // If answer change is already in sync queue, update that answer instead of appending new one
      const existingAnswerIdx = this.syncActionQueue.findIndex(queuedAction => {
        return queuedAction.type === action.type && queuedAction.id === action.id;
      });

      if (existingAnswerIdx !== -1) {
        this.syncActionQueue[existingAnswerIdx] = action;
      } else {
        add = true;
      }
      // In cases where server response is required, only add the action to queue once. Otherwise
      // you can create a situation where user clicks something multiple times because nothing is
      // happening on screen and then once sync succeeds, all the queued actions create a very
      // unexpected state on user's screen
    } else if (action.type === 'ADD_ROW') {
      add = !this.syncActionQueue.some(queuedAction =>
        queuedAction.type === action.type && queuedAction.id === action.id
      );
    } else if (action.type === 'NEXT' || action.type === 'PREVIOUS' || action.type === 'GOTO') {
      add = !this.syncActionQueue.some(queuedAction => queuedAction.type === action.type);
    } else {
      add = true;
    }

    if (add) {
      this.syncActionQueue.push(action);
    }
  }


  private syncQueuedActions = async (): Promise<void> => {
    this.clearDeferredSync();
    if (this.inSync || this.syncActionQueue.length === 0) {
      return;
    }

    this.inSync = true;
    const syncedActions = this.syncActionQueue;
    const syncImmediately = this.syncQueueImmediately;
    this.syncActionQueue = [];
    this.syncQueueImmediately = false;
    try {
      await this.runSyncFn(async () => {
        const response = await this.options.fetchActionPost(this.id, syncedActions, this.rev);
        if (!response.ok) {
          throw new ActionsQueueRequestError('Failure during fetch', response.status);
        }
        const json = await response.json();
        return json;
      });
      this.inSync = false;
      this.retryCount = 0;

      if (this.syncActionQueue.length > 0 && !this.syncTimer) {
        this.syncQueuedActions();
      }
    } catch (e: any) {
      this.handleError(e);
      const newActions = this.syncActionQueue;
      this.syncActionQueue = syncedActions;
      for (const action of newActions) {
        this.addToSyncQueue(action);
      }
      this.inSync = false;
      this.syncQueueImmediately = this.syncQueueImmediately || syncImmediately;
      this.retryCount++;

      if (!this.syncTimer) {
        this.deferSync(1000);
      }

      if (this.retryCount >= 3) {
        this.options.error.forEach(l => l({ type: 'SYNC-REPEATED', error: e }));
      }
    }
  }

  private deferSync(timeout = this.options.syncWait) {
    this.syncTimer = setTimeout(this.syncQueuedActions, timeout);
  }

  private clearDeferredSync() {
    if (!this.syncTimer) return;

    clearTimeout(this.syncTimer);
    this.syncTimer = undefined;
  }

  private async runSyncFn(syncFn: () => Promise<ActionsQueueResponse>): Promise<ActionsQueueResponse> {
    this.options.sync.forEach(l => l({syncState: 'INPROGRESS'}));
    const response = await syncFn();
    this.rev = response.rev;
    this.options.sync.forEach(l => l({syncState:'DONE', response}));
    return response;
  }

  private handleError(error: Error) {
    if (error.name !== 'NetworkError' && error.name !== 'ActionsQueueRequestError') {
      throw error;
    }
    this.options.error.forEach(l => l({ type: 'SYNC', error }));
  }
}


function syncActionImmediately(action: DialobApi.Action): boolean {
  return action.type === 'ADD_ROW' || action.type === 'NEXT' || action.type === 'PREVIOUS' || action.type === 'GOTO' || action.type === 'SET_LOCALE';
}


export class ActionsQueueError extends Error {
  constructor(reason: string) {
    super(reason);
    Object.setPrototypeOf(this, ActionsQueueError.prototype);
  }
}

export class ActionsQueueRequestError extends Error {
  reason: string;
  code: number;
  constructor(reason: string, code: number) {
    super(reason);

    Object.setPrototypeOf(this, ActionsQueueRequestError.prototype);
    this.reason = reason;
    this.code = code;
    this.name = 'ActionsQueueRequestError';
  }
}
