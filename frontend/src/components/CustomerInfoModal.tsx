import { X } from 'lucide-react'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'

interface Props {
  open: boolean
  userId: string
  email: string
  onClose: () => void
}

export default function CustomerInfoModal({ open, userId, email, onClose }: Readonly<Props>) {
  return (
    <Dialog open={open} onOpenChange={open => { if (!open) onClose() }}>
      <DialogContent className="max-w-sm p-6">
        <DialogHeader className="flex flex-row items-center justify-between space-y-0">
          <DialogTitle className="text-base">Customer info</DialogTitle>
          <button
            onClick={onClose}
            className="rounded-sm opacity-70 hover:opacity-100 transition-opacity"
          >
            <X className="h-4 w-4" />
          </button>
        </DialogHeader>
        <div className="grid grid-cols-[auto_1fr] gap-x-6 gap-y-2 text-sm">
          <span className="text-muted-foreground">UUID</span>
          <span className="font-mono text-xs break-all">{userId}</span>

          <span className="text-muted-foreground">Email</span>
          <span>{email}</span>
        </div>
      </DialogContent>
    </Dialog>
  )
}
