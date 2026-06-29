import { CircleCheck, Info, LoaderCircle, OctagonX, TriangleAlert } from "lucide-react"
import { Toaster as Sonner } from "sonner"

type ToasterProps = React.ComponentProps<typeof Sonner>

const Toaster = ({ ...props }: ToasterProps) => {
  return (
    <Sonner
      theme="light"
      className="toaster group"
      icons={{
        success: <CircleCheck className="h-4 w-4" />,
        info: <Info className="h-4 w-4" />,
        warning: <TriangleAlert className="h-4 w-4" />,
        error: <OctagonX className="h-4 w-4" />,
        loading: <LoaderCircle className="h-4 w-4 animate-spin" />,
      }}
      toastOptions={{
        classNames: {
          toast:
            "group toast group-[.toaster]:bg-background group-[.toaster]:text-foreground group-[.toaster]:border-border group-[.toaster]:shadow-lg group-[.toaster]:border-l-4",
          success:
            "group-[.toaster]:border-l-green-500 group-[.toaster]:text-green-700 [&>[data-icon]]:text-green-500",
          error:
            "group-[.toaster]:border-l-red-500 group-[.toaster]:text-red-700 [&>[data-icon]]:text-red-500",
          warning:
            "group-[.toaster]:border-l-yellow-500 group-[.toaster]:text-yellow-700 [&>[data-icon]]:text-yellow-500",
          info:
            "group-[.toaster]:border-l-blue-500 group-[.toaster]:text-blue-700 [&>[data-icon]]:text-blue-500",
          description: "group-[.toast]:text-muted-foreground",
          actionButton:
            "group-[.toast]:bg-primary group-[.toast]:text-primary-foreground",
          cancelButton:
            "group-[.toast]:bg-muted group-[.toast]:text-muted-foreground",
        },
      }}
      {...props}
    />
  )
}

export { Toaster }
