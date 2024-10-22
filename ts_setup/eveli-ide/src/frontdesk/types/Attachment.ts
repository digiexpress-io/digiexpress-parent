export interface Attachment {
  name: string;
  status: 'OK'|'QUARANTINED'|'UPLOADED';
  created: Date;
  updated: Date;
  size: number;
}

export interface AttachmentUploadResponse {
  putRequestUrl: string
}