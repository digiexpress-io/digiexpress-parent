import { produce } from 'immer';
import getBy from './getBy';

function get(entity:any, key:string|string[]){
  if(Array.isArray(key)) {
    return getBy(entity, key);
  }
  return entity[key];
}

export default function mapNestedEntities(
  entities:any[],
  mainKey:string|string[],
  parentKey:string|string[]
){
  return produce(entities, entities => {
    const map = new Map();
    entities.forEach(entity => map.set(get(entity, mainKey), entity));

    for(let i = 0; i < entities.length; i++) {
      const entity = entities[i];
      const parent = map.get(get(entity, parentKey));
      if(!parent) {
        continue;
      }

      entity.__parent = parent;
      if(!parent.__children) parent.__children = [];
      parent.__children.push(entity);

      // delete from root array since this is not a root node
      entities.splice(i, 1);
      i--;
    }
  });
}