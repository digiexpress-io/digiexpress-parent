import { ClassifierType, DataType, Collection, Group, ExtractClassifier, OriginIndex } from './grouping-types';

import LoggerFactory from 'logger';

const _logger = LoggerFactory.getLogger();

interface Init<D extends DataType> {
  origin: readonly D[];
  classifierName: string, 
  definition: ExtractClassifier<D>;
  groupValues: ClassifierType[];

  cache?: Record<ClassifierType, InternalCache<D>>;
  groups?: readonly Group[];
}


interface InternalCache<D extends DataType> {
  classifierName: string; 
  definition: ExtractClassifier<D>;
  groups: readonly Group[];
}

class ImmutableGroup implements Group {
  private _id: ClassifierType;
  private _value: readonly OriginIndex[];

  constructor(init: {id: ClassifierType, value: OriginIndex[]}) {
    this._id = init.id;
    this._value = Object.freeze(init.value);
  }

  get id() { return this._id; }
  get value() { return this._value; }
}


export class ImmutableCollection<D extends DataType> implements Collection<D> {
  private _origin: readonly D[];
  private _groups: readonly Group[];
  private _cache: Record<ClassifierType, InternalCache<D>> = {} as any;
  private _classifierName: string;
  private _definition: ExtractClassifier<D>;
  private _groupValues: string[];

  constructor(raw: Init<D>) {
    _logger.target(raw).debug("loading grouping");
    this._origin = raw.origin;
    this._classifierName = raw.classifierName;
    this._definition = raw.definition;
    this._groupValues = raw.groupValues;

    if(raw.groups === undefined && raw.cache === undefined) {
      const init = new GroupingVisitor(raw.origin, {}, raw.classifierName, raw.definition, raw.groupValues).visit();
      this._groups = Object.freeze(init.groups ?? []);
      this._cache = Object.freeze(init.cache ?? {});
    } else {
      this._groups = Object.freeze(raw.groups ?? []);
      this._cache = Object.freeze(raw.cache ?? {});
    }
  }
  get origin() { return this._origin }
  get groups() { return this._groups }
  get classifierName() { return this._classifierName }

  withGroupBy(classifierName: string, definition: ExtractClassifier<D>, groupValues: string[]): ImmutableCollection<D> {
    if(this._cache[classifierName] && this._cache[classifierName].definition === definition) {
      return this;
    }
    const reinit = new GroupingVisitor(this._origin, this._cache, classifierName, definition, groupValues).visit();
    return new ImmutableCollection(reinit);
  }

  withOrigin(newOrigin: (D[] | readonly D[])) {
    const reinit = new GroupingVisitor(newOrigin, {}, this._classifierName, this._definition, this._groupValues).visit();
    return new ImmutableCollection(reinit);
  }
}



class GroupingVisitor<D extends DataType> {
  private _origin: readonly D[];
  private _cache: Record<string, InternalCache<D>>;
  private _classifierName: string; 
  private _definition: ExtractClassifier<D>;
  private _groupValues: string[];

  constructor(
    origin: readonly D[], 
    cache: Record<ClassifierType, InternalCache<D>>, 
    classifierName: string,
    definition: ExtractClassifier<D>,
    groupValues: string[]
  ) {
    this._classifierName = classifierName;
    this._definition = definition;
    this._origin = origin;
    this._cache = cache;
    this._groupValues = groupValues;
  }

  visit(): Init<D> {
    const classifierName = this._classifierName;
    const definition = this._definition;
    const dirty_groups: Record<ClassifierType, number[]> = {};

    for(const classifier of this._groupValues) {
      if(!dirty_groups[classifier]) {
        dirty_groups[classifier] = [];
      }      
    }

    let runningIndex = -1;
    for(const entry of this._origin) {
      runningIndex++;

      const classifier: string | undefined = definition(entry);
      if(classifier === undefined) {
        continue
      }
      
      if(!dirty_groups[classifier]) {
        dirty_groups[classifier] = [];
      }
      dirty_groups[classifier].push(runningIndex);
    }

    const groups = Object.freeze(Object.entries(dirty_groups).map(([id, value]) => new ImmutableGroup({id, value})));
    const cache: Record<string, InternalCache<D>> = {...this._cache};
    cache[classifierName] = { groups, classifierName, definition };

    return {
      classifierName: this._classifierName,
      definition: this._definition,
      origin: this._origin,
      groupValues: this._groupValues,
      groups, cache
    };
  }

  private visitData(data: D) {
      
  }
}