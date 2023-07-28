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
  attachments: Attachment[]
  read: boolean
  replyToId?: string
}

interface Attachment {
  id: string
  name: string
}

interface ThreadPreviewProps {
  thread: Thread,
  onClick: (thread: Thread) => void
}

type TabType = 'messages' | 'attachments' | 'form'

export type { Thread, Message, Attachment, ThreadPreviewProps, TabType }
