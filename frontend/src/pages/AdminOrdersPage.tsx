import { useState } from 'react'
import { ArrowDown, ArrowUp, ChevronDown } from 'lucide-react'
import { useAdminOrders } from '../hooks/useOrders'
import OrderStatusBadge from '../components/OrderStatusBadge'
import OrderDetailsModal from '../components/OrderDetailsModal'
import CustomerInfoModal from '../components/CustomerInfoModal'
import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { Checkbox } from '@/components/ui/checkbox'
import { Label } from '@/components/ui/label'
import type { AdminOrderResponse } from '../api/orders'

const ALL_STATUSES = ['PLACED', 'CONFIRMED', 'PREPARING', 'READY', 'PICKED_UP', 'DELIVERED', 'CANCELLED']

function formatStatus(s: string) {
  return s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ')
}

const SELECT_CLASS = 'h-8 rounded-md border border-input bg-background px-3 text-sm'

export default function AdminOrdersPage() {
  const [selectedStatuses, setSelectedStatuses] = useState<string[]>([])
  const [emailFilter, setEmailFilter] = useState('')
  const [emailInput, setEmailInput] = useState('')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTill, setDateTill] = useState('')
  const [sortBy, setSortBy] = useState('')
  const [sortDir, setSortDir] = useState('')
  const [selectedOrder, setSelectedOrder] = useState<AdminOrderResponse | null>(null)
  const [customerInfoOpen, setCustomerInfoOpen] = useState(false)

  const toggleStatus = (status: string) => {
    setSelectedStatuses(prev =>
      prev.includes(status) ? prev.filter(s => s !== status) : [...prev, status]
    )
  }

  const filter = {
    statuses: selectedStatuses.length > 0 ? selectedStatuses : undefined,
    userEmailSearch: emailFilter || undefined,
    dateFrom: dateFrom || undefined,
    dateTill: dateTill || undefined,
    sortBy: sortBy || undefined,
    sortDir: sortDir || undefined,
  }

  const { data: orders, isLoading, isError } = useAdminOrders(filter)

  const handleEmailSearch = () => setEmailFilter(emailInput.trim())

  return (
    <Card>
      <CardHeader className="px-5 py-3 border-b space-y-0">
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <Input
              placeholder="Search by customer email..."
              value={emailInput}
              onChange={e => setEmailInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleEmailSearch()}
              onBlur={handleEmailSearch}
              className="h-8 text-sm w-56"
            />
            <Popover>
              <PopoverTrigger asChild>
                <Button variant="outline" size="sm" className="h-8 gap-1.5">
                  {selectedStatuses.length === 0 ? 'All statuses' : `${selectedStatuses.length} selected`}
                  <ChevronDown className="h-3.5 w-3.5 text-muted-foreground" />
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-48 p-2" align="start">
                <div className="space-y-1">
                  {ALL_STATUSES.map(s => (
                    <div key={s} className="flex items-center gap-2 px-1 py-1 rounded hover:bg-muted/50">
                      <Checkbox
                        id={`status-${s}`}
                        checked={selectedStatuses.includes(s)}
                        onCheckedChange={() => toggleStatus(s)}
                      />
                      <Label htmlFor={`status-${s}`} className="text-sm font-normal cursor-pointer">
                        {formatStatus(s)}
                      </Label>
                    </div>
                  ))}
                  {selectedStatuses.length > 0 && (
                    <>
                      <div className="border-t my-1" />
                      <button
                        onClick={() => setSelectedStatuses([])}
                        className="w-full text-left px-1 py-1 text-xs text-muted-foreground hover:text-foreground transition-colors"
                      >
                        Clear selection
                      </button>
                    </>
                  )}
                </div>
              </PopoverContent>
            </Popover>
          </div>

          <div className="flex items-center gap-2">
            <div className="flex items-center gap-1.5">
              <Input
                type="date"
                value={dateFrom}
                onChange={e => setDateFrom(e.target.value)}
                className="h-8 text-sm w-36"
              />
              <span className="text-xs text-muted-foreground">-</span>
              <Input
                type="date"
                value={dateTill}
                onChange={e => setDateTill(e.target.value)}
                className="h-8 text-sm w-36"
              />
            </div>

            <div className="flex items-center gap-2 ml-auto">
              <select value={sortBy} onChange={e => setSortBy(e.target.value)} className={SELECT_CLASS}>
                <option value="">Sort by</option>
                <option value="CREATED_AT">Created at</option>
                <option value="LAST_MODIFIED_AT">Modified at</option>
                <option value="TOTAL_PRICE">Total price</option>
              </select>
              <Button
                variant="outline"
                size="sm"
                className="h-8 w-8 p-0"
                onClick={() => setSortDir(prev => prev === 'ASC' ? 'DESC' : 'ASC')}
                title={sortDir === 'ASC' ? 'Ascending' : 'Descending'}
              >
                {sortDir === 'ASC' ? <ArrowUp className="h-3.5 w-3.5" /> : <ArrowDown className="h-3.5 w-3.5" />}
              </Button>
            </div>
          </div>
        </div>
      </CardHeader>

      <CardContent className="p-0">
        {isLoading && <p className="px-5 py-8 text-sm text-muted-foreground text-center">Loading...</p>}
        {isError && <p className="px-5 py-8 text-sm text-destructive text-center">Something went wrong.</p>}
        {!isLoading && !isError && orders && (
          orders.length === 0
            ? <p className="px-5 py-8 text-sm text-muted-foreground text-center">No orders found.</p>
            : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-xs text-muted-foreground uppercase tracking-wide border-b">
                    <th className="px-5 py-3 font-medium">Order ID</th>
                    <th className="px-5 py-3 font-medium">Customer Email</th>
                    <th className="px-5 py-3 font-medium">Date</th>
                    <th className="px-5 py-3 font-medium">Time</th>
                    <th className="px-5 py-3 font-medium">Total</th>
                    <th className="px-5 py-3 font-medium">Status</th>
                    <th className="px-5 py-3" />
                  </tr>
                </thead>
                <tbody className="divide-y">
                  {orders.map(order => {
                    const date = new Date(order.orderData.createdAt)
                    return (
                      <tr key={order.orderData.id} className="hover:bg-muted/30 transition-colors">
                        <td className="px-5 py-3 font-mono text-xs text-muted-foreground">
                          {order.orderData.id.slice(0, 6)}...
                        </td>
                        <td className="px-5 py-3 text-xs text-muted-foreground">
                          {order.customerEmail}
                        </td>
                        <td className="px-5 py-3 whitespace-nowrap">
                          {date.toLocaleDateString()}
                        </td>
                        <td className="px-5 py-3 whitespace-nowrap text-muted-foreground">
                          {date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </td>
                        <td className="px-5 py-3 whitespace-nowrap">{order.orderData.totalPrice.toFixed(2)} €</td>
                        <td className="px-5 py-3">
                          <OrderStatusBadge status={order.orderData.status} />
                        </td>
                        <td className="px-5 py-3">
                          <Button size="sm" variant="outline" className="h-7 text-xs" onClick={() => setSelectedOrder(order)}>
                            View
                          </Button>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            )
        )}
      </CardContent>

      <OrderDetailsModal
        order={selectedOrder}
        onClose={() => setSelectedOrder(null)}
        onCustomerInfoOpen={() => setCustomerInfoOpen(true)}
      />
      <CustomerInfoModal
        open={customerInfoOpen}
        userId={selectedOrder?.orderData.userId ?? ''}
        email={selectedOrder?.customerEmail ?? ''}
        onClose={() => setCustomerInfoOpen(false)}
      />
    </Card>
  )
}
