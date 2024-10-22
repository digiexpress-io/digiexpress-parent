
export enum CommentSource {
  FRONTDESK="FRONTDESK",
  PORTAL="PORTAL"
}
export interface Comment {
  id: number
  userName: string
  created: string
  commentText: string
  replyToId?: number|null
  // added in UI for hierarchical display
  __parent?: Comment
  __children?: Comment[]
  external?: boolean
  source?: CommentSource
}
