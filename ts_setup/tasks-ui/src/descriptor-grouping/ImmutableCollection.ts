import { ClassifierType, DataType, Collection, Group, ExtractClassifier, OriginIndex } from './grouping-types';


interface Init<D extends DataType> {
  origin: readonly D[];
  classifierName: string, 
  definition: ExtractClassifier<D>;

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

  constructor(init: Init<D>) {
    this._origin = init.origin;
    this._classifierName = init.classifierName;
    this._definition = init.definition;
    this._groups = Object.freeze(init.groups ?? []);
    this._cache = Object.freeze(init.cache ?? {});
  }
  get origin() { return this._origin }
  get groups() { return this._groups }
  get classifierName() { return this._classifierName }

  withGroupBy(classifierName: string, definition: ExtractClassifier<D>): ImmutableCollection<D> {
    if(this._cache[classifierName] && this._cache[classifierName].definition === definition) {
      return this;
    }
    const reinit = new GroupingVisitor(this._origin, this._cache, classifierName, definition).visit();
    return new ImmutableCollection(reinit);
  }

  withOrigin(newOrigin: D[]) {
    const reinit = new GroupingVisitor(newOrigin, {}, this._classifierName, this._definition).visit();
    return new ImmutableCollection(reinit);
  }
}



class GroupingVisitor<D extends DataType> {
  private _origin: readonly D[];
  private _cache: Record<string, InternalCache<D>>;
  private _classifierName: string; 
  private _definition: ExtractClassifier<D>;

  constructor(
    origin: readonly D[], 
    cache: Record<ClassifierType, InternalCache<D>>, 
    classifierName: string,
    definition: ExtractClassifier<D>
  ) {
    this._classifierName = classifierName;
    this._definition = definition;
    this._origin = origin;
    this._cache = cache;
  }

  visit(): Init<D> {
    const classifierName = this._classifierName;
    const definition = this._definition;

    const dirty_groups: Record<ClassifierType, number[]> = {};

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
      groups, cache
    };
  }

  private visitData(data: D) {
      
  }
}