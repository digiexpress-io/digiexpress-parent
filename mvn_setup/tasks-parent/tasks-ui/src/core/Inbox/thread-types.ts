import { TaskExtension } from "taskclient/task-types"

interface Thread {
  id: string
  userName: string
  topicName: string
  formName: string
  messages: Message[]
}

interface Message {
  id: string
  userName: string
  representerName?: string
  text: string
  date: Date
  attachments: TaskExtension[]
  read: boolean
  replyToId?: string
}

interface ThreadPreviewProps {
  thread: Thread,
  active: boolean,
  onClick: (thread: Thread) => void
}

type TabType = 'messages' | 'attachments' | 'form'

export type { Thread, Message, ThreadPreviewProps, TabType }
