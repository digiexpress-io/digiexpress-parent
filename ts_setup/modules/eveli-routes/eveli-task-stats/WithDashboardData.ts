import { TaskApi } from "@dxs-ts/eveli-api";
import { OVERDUE_FILL_COLORS } from "./useUtilityClasses";
import { DateTime, Interval } from "luxon";

export interface DashboardEvent extends TaskApi.GrimMissionAttributeEvent {
  intl: string;
  fill: string;
}



export type DashboardGroupEvent = { 
  eventDate: string; 
  eventAgeInMonths: number; 
  [key: string]: number | string;
};


export class WithDashboardData {
  private _init: TaskApi.TaskDasboard;
  private _predicate?: (item: TaskApi.GrimMissionAttributeEvent) => boolean;
  private _intl?: (item: TaskApi.GrimMissionAttributeEvent) => string;
  private _fill?: (item: TaskApi.GrimMissionAttributeEvent) => string;
  private _now = DateTime.fromJSDate(new Date());

  constructor(init: TaskApi.TaskDasboard) {
    this._init = init;
  }
  filter(predicate: (item: TaskApi.GrimMissionAttributeEvent) => boolean) {
    this._predicate = predicate;
    return this;
  }
  intl(localeMapper: (item: TaskApi.GrimMissionAttributeEvent) => string) {
    this._intl = localeMapper;
    return this;
  }
  fill(fillColorMapper: (item: TaskApi.GrimMissionAttributeEvent) => string) {
    this._fill = fillColorMapper;
    return this;
  }
  groupByDate(mapper: (items: DashboardGroupEvent[]) => React.ReactElement ): React.ReactElement {
    const initialItems = this._init.events
      .filter(event => this._predicate ? this._predicate(event) : false)
      .filter(event => event.eventDate);

    const initObject = initialItems.reduce<Record<string, number>>((collector, current) => {
        const key = current.attributeValue.toLowerCase() as string;
        collector[key] = 0;
        return collector;
    }, {});      

    const result = initialItems.reduce<Record<string, DashboardGroupEvent>>((collector, current) => {
      if(!current.eventDate) {
        return collector;
      }

      // init if undefined
      if(!collector[current.eventDate]) {
        const eventAgeInMonths = Interval.fromDateTimes(
          DateTime.fromISO(current.eventDate),
          this._now
        ).length('months')

        const newValue: DashboardGroupEvent = { 
          ...initObject, 
          eventDate: current.eventDate as string,
          eventAgeInMonths
        }
        collector[current.eventDate] = newValue;
      }

      const target: DashboardGroupEvent = collector[current.eventDate];
      const key = current.attributeValue.toLowerCase() as string;
      target[key] = (target[key] as number ?? 0) + current.eventCount;

      return collector;
    }, {});
    
    return mapper(
      Object.values(result).sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime())
    );
  }
  map(mapper: (items: DashboardEvent[]) => React.ReactNode): React.ReactNode  {
    const items = this._init.events
      .filter(event => this._predicate ? this._predicate(event) : false)
      .map((event, index) => ({
        ...event,
        intl: this._intl ? this._intl(event) : event.attributeValue,
        fill: this._fill ? this._fill(event) : OVERDUE_FILL_COLORS[index % OVERDUE_FILL_COLORS.length]
      }));
    return mapper(items);
  }
}

export function withDs(init: TaskApi.TaskDasboard) {
  return new WithDashboardData(init);
}