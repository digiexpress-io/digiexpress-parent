
export type EVENT_TYPE = 'EVENT_AM_UPDATE';

export interface BackendEvent {
  type: EVENT_TYPE;
}

export interface AmUpdateEvent extends BackendEvent {
  type: 'EVENT_AM_UPDATE';
}