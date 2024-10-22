export default function getBy(item:object, path:string|string[]) {
  if(typeof path === 'string') {
    path = [path];
  }

  return (path.reduce((val:any, field:string) => {
    if(!val) return undefined;
    return val[field];
  }, item));
}
