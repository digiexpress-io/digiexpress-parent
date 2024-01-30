export type ClassifierType = string;
export type DataType = { }
export type ExtractClassifier<D extends DataType> = (data: D) => ClassifierType | ClassifierType[] | undefined;
export type OriginIndex = number;


export interface Collection<D extends DataType> {
  classifierName: string;
  origin: readonly D[];
  groups: readonly Group[];
  updated: string; 
}

export interface Group {
  id: ClassifierType;
  value: readonly OriginIndex[];
}

export interface GroupReducer<D extends DataType> {
  withData: (newInput: (D[] | readonly D[])) => void;
  withGroupBy: (classifierName: string, definition: ExtractClassifier<D>, groupValues: ClassifierType[]) => void;
}