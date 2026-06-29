import { Trash2 } from 'lucide-react'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'

interface Props {
  open: boolean
  title: string
  description?: string
  onConfirm: () => void
  onCancel: () => void
  isPending?: boolean
}

export default function ConfirmDialog({ open, title, description, onConfirm, onCancel, isPending }: Props) {
  return (
    <Dialog open={open} onOpenChange={open => { if (!open) onCancel() }}>
      <DialogContent className="max-w-sm p-0 overflow-hidden">
        <DialogHeader className="px-6 pt-6 pb-4">
          <div className="flex items-start gap-4">
            <div className="flex-shrink-0 w-10 h-10 rounded-full bg-red-50 flex items-center justify-center">
              <Trash2 className="w-5 h-5 text-red-500" />
            </div>
            <div className="space-y-1 pt-1">
              <DialogTitle className="text-base">{title}</DialogTitle>
              {description && (
                <DialogDescription className="text-sm">{description}</DialogDescription>
              )}
            </div>
          </div>
        </DialogHeader>
        <DialogFooter className="px-6 py-4 bg-muted/40 border-t flex flex-row justify-end gap-2 sm:space-x-0">
          <Button variant="outline" size="sm" onClick={onCancel} disabled={isPending}>
            Cancel
          </Button>
          <Button variant="destructive" size="sm" onClick={onConfirm} disabled={isPending}>
            {isPending ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
