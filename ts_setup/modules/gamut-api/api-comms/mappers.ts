import { DateTime } from 'luxon';

import { Md5 } from 'ts-md5';
import { CommsApi } from './comms-types';
import { LegacyProcessApi } from '../api-legacy-processes';
import { IamApi } from '../api-iam';


export function mapToSubjectData(data: LegacyProcessApi.Process[], user: IamApi.User | undefined): {
  hash: string;
  subjects: readonly CommsApi.Subject[];
} {
  const md5 = new Md5();
  const subjects: CommsApi.Subject[] = [];

  for (const proc of data) {
    if (!proc.taskId) {
      continue;
    }
    if (proc.messages?.length === 0) {
      //continue;
    }

    const contract = mapToSubject(proc, user);
    md5
      .appendStr(proc.id)
      .appendStr(proc.taskStatus ?? '')
      .appendStr(proc.reviewUri ?? '')
      .appendStr(proc.taskCreated ?? '')
      .appendStr(proc.viewed + '')
      .appendStr(proc.taskUpdated ?? '');

    for (const doc of contract.documents) {
      md5
        .appendStr(doc.created.toISO() ?? '')
        .appendStr(doc.id)
        .appendStr(doc.name)
        .appendStr(doc.size + '')
    }

    subjects.push(contract);
  }

  return { subjects: Object.freeze(subjects), hash: md5.end() + '' };
}

function mapToSubject(data: LegacyProcessApi.Process, user: IamApi.User | undefined): CommsApi.Subject {
  const documents: readonly CommsApi.SubjectDocument[] = Object.freeze(data.attachments.map(({ id, name, size, created }) => Object.freeze({
    id,
    name,
    size,
    created: DateTime.fromISO(created)
  })));

  // Build a set of identifiers that might appear in message.userName for the current user
  const myNames = new Set(
    [
      user?.userId,
      (user as any)?.userName,
      (user as any)?.username,
      (user as any)?.name,
      (user as any)?.email,
    ]
      .filter((v): v is string => typeof v === 'string' && v.length > 0)
  );

  const exchange: readonly CommsApi.Message[] = data.messages
    .map(m => Object.freeze({
      id: m.id,
      created: DateTime.fromISO(m.created),
      replyToId: m.replyToId,
      commentText: m.commentText,
      userName: m.userName,
      // BEFORE: isMyMessage: m.userName === (user?.userId ?? '')
      isMyMessage: myNames.has(m.userName)
    }))
    .sort((a, b) => a.created.toMillis() - b.created.toMillis());

  let created: DateTime;
  let updated: DateTime;

  const lastExchange = exchange[exchange.length - 1];
  if (exchange.length) {
    created = exchange[0]?.created;
    updated = lastExchange?.created;
  } else if (data.taskId) {
    created = DateTime.fromISO(data.taskCreated!);
    updated = DateTime.fromISO(data.taskUpdated!);
  } else {
    created = DateTime.fromISO(data.created);
    updated = DateTime.fromISO(data.updated);
  }

  // If there are messages, consider the thread viewed only if the last message is mine.
  const isViewed: boolean = data.viewed;


  return Object.freeze({
    id: data.id,
    name: data.name,
    contractId: data.taskId!,
    product: {} as any,
    formId: data.formId,
    documents,
    exchange,
    lastExchange,
    created,
    updated,
    isViewed
  });

}