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
  updated?: string;
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
  private _updated: string;

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
      this._updated = new Date().toISOString();
    } else {
      this._groups = Object.freeze( [...(raw.groups ?? [])].sort((a, b) => a.id.localeCompare(b.id)));
      this._cache = Object.freeze(raw.cache ?? {});
      this._updated = raw.updated ?? new Date().toISOString();
    }
  }
  get origin() { return this._origin }
  get groups() { return this._groups }
  get updated() { return this._updated }
  get classifierName() { return this._classifierName }

  withGroupBy(classifierName: string, definition: ExtractClassifier<D>, groupValues: string[]): ImmutableCollection<D> {
    if(this._cache[classifierName] && this._cache[classifierName].definition === definition) {
      return this;
    }
    const reinit = new GroupingVisitor(this._origin, this._cache, classifierName, definition, groupValues).visit();
    reinit.updated = this._updated;
    return new ImmutableCollection(reinit);
  }

  withOrigin(newOrigin: (D[] | readonly D[])) {
    const reinit = new GroupingVisitor(newOrigin, {}, this._classifierName, this._definition, this._groupValues).visit();
    reinit.updated = new Date().toISOString();
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

      const classifier: string | string[] | undefined = definition(entry);
      if(classifier === undefined) {
        continue
      }

      const classifiers: string[] = Array.isArray(classifier) ? classifier : [classifier];
      for(const resolved of classifiers) {
        if(!dirty_groups[resolved]) {
          dirty_groups[resolved] = [];
        }
        dirty_groups[resolved].push(runningIndex);
      }
    }

    const groups = Object.freeze(Object
      .entries(dirty_groups)
      .map(([id, value]) => new ImmutableGroup({id, value}))
      .sort((a, b) => a.id.localeCompare(b.id))
    );
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
}