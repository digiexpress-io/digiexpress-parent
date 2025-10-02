import React from 'react';
import { GInputTextArea } from './GInputTextArea';

export const GInputTextAreaDialob: React.FC<any> = ({
  disabled,
  actionItem: element,
  formStore: store,
}) => {
  const MIN_ROWS = 1;
  const MAX_ROWS = 20;

  const clamp = (n: number, min = MIN_ROWS, max = MAX_ROWS) =>
    Math.max(min, Math.min(max, n));

  function safeParseRows(raw: unknown): number | undefined {
    try {
      if (typeof raw === 'number' && Number.isFinite(raw)) {
        return clamp(Math.floor(raw));
      }
      if (typeof raw === 'string') {
        const trimmed = raw.trim();
        const n = Number(trimmed);
        if (Number.isFinite(n)) {
          return clamp(Math.floor(n));
        }
      }
    } catch {
      // swallow and return undefined to use default
    }
    return undefined;
  }

  const errors = store.form.toErrors(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);

  const onChange = (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    store.setAnswer(element.id, event.target.value);
  };

  const rawRows = (element as any)?.properties?.rows;
  const rows = safeParseRows(rawRows);

  return (
    <GInputTextArea
      id={element.id}
      disabled={disabled}
      label={element.label}
      description={desc}
      errors={errors}
      required={!!element.required}
      value={element.value ?? ''}
      variant="textBox"
      onChange={onChange}
      labelPosition={labelPosition}
      rows={rows}
    />
  );
};

export default GInputTextAreaDialob;
