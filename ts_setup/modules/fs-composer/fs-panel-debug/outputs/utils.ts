import YAML from 'yaml';

const toYaml = (props: any) => {
  const doc = new YAML.Document();
  doc.contents = props;
  return doc.toString();
}

const removeEmpty = (obj: any): any => {
  return Object.entries(obj)
    .filter(([_, v]) => v != null)
    .reduce(
      (acc, [k, v]) => ({ ...acc, [k]: v === Object(v) ? removeEmpty(v) : v }),
      {}
    );
}

const LINE_HEIGHT = 19;
const MIN_EDITOR_HEIGHT = 57;   // 3 lines
const MAX_EDITOR_HEIGHT = 500;

const calcEditorHeight = (yaml: string): number => {
  const lines = yaml.split('\n').length;
  return Math.min(MAX_EDITOR_HEIGHT, Math.max(MIN_EDITOR_HEIGHT, lines * LINE_HEIGHT));
};

export { toYaml, calcEditorHeight };
