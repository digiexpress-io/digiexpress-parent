import React from 'react';

import { Collection, ClassifierType, DataType, ExtractClassifier, GroupReducer } from './grouping-types';
import { ImmutableCollection } from './ImmutableCollection';


export function initReducer<D extends DataType>(
  setCollection: React.Dispatch<React.SetStateAction<ImmutableCollection<D>>>,
): GroupReducer<D> {

  const result: GroupReducer<D> = {
    withData(newInput) {
      setCollection(prev => prev.withOrigin(newInput));
    },
    withGroupBy(classifierName, definition) {
      setCollection(prev => prev.withGroupBy(classifierName, definition));
    },
  };
  return Object.freeze(result);
}


// build the group
export interface GroupingFactory<D extends DataType> {
  groupBy: (classifierName: string, definition: ExtractClassifier<D>) => GroupingBuilder<D>;
}
export interface GroupingBuilder<D extends DataType> {
  collect: () => Collection<D>;
}


export function getInstance<D extends DataType>(origin: D[]): GroupingFactory<D> {
  function groupBy(classifierName: string, definition: ExtractClassifier<D>): GroupingBuilder<D> {
    return Object.freeze({
      collect() {
        return new ImmutableCollection({ classifierName, definition, origin });  
      },
    });
  }

  return Object.freeze({ groupBy });
}