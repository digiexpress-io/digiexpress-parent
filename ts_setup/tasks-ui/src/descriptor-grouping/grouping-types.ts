export type ClassifierType = string;
export type DataType = { }
export type ExtractClassifier<D extends DataType> = (data: D) => ClassifierType | undefined;
export type OriginIndex = number;


export interface Collection<D extends DataType> {
  classifierName: string;
  origin: readonly D[];
  groups: readonly Group[];
}

export interface Group {
  id: ClassifierType;
  value: readonly OriginIndex[];
}

export interface GroupReducer<D extends DataType> {
  withData: (newInput: (D[] | readonly D[])) => void;
  withGroupBy: (classifierName: string, definition: ExtractClassifier<D>, groupValues: ClassifierType[]) => void;
}

/**
  join: (ext: JoinExtensions<D>) => GroupingBuilder<D>;
 * export interface JoinExtensions<D extends DataType> {
  avatars?: { 
    field?: (keyof D), 
    resolver?: (data: D) => string 
  }[];
}
export interface Sorting<D extends DataType> {
  field?: (keyof D),
  direction?: 'ASC' | 'DESC',
  compareFn?: (a: D, b: D) => number,
}
//sortGroup: (classifierValue: C, def: (Sorting<D>[] | Sorting<D>)) => GroupingBuilder<D, C>;
*/



/*
 * normal use case
const data: { id: string, status: "OPEN" | "CLOSED"}[] = [];
const factory: getInstance = {} as any;

factory(data).groupBy("status", (entry) => entry.status).collect();
*/
