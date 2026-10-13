import {
  Bell,
  Globe2,
  LockKeyhole,
  Monitor,
  Moon,
  Palette,
  Shield,
  Sun,
  UserRound,
} from "lucide-react"
import { useState } from "react"
import PageHeader from "../components/common/PageHeader"
import { Button, Card, Input, Select, Textarea } from "../components/common/ui"
import { useAppStore } from "../store/useAppStore"

const tabs = [
  { id: "general", label: "General", icon: Globe2 },
  { id: "profile", label: "Profile", icon: UserRound },
  { id: "security", label: "Security", icon: LockKeyhole },
  { id: "notifications", label: "Notifications", icon: Bell },
  { id: "appearance", label: "Appearance", icon: Palette },
]
function Toggle({
  checked,
  onChange,
}: {
  checked: boolean
  onChange: () => void
}) {
  return (
    <Button
      variant="ghost"
      className={`h-6 min-h-6 w-11 rounded-full p-0 ${
        checked ? "bg-primary hover:bg-primary-hover" : "bg-muted"
      }`}
      onClick={onChange}
      role="switch"
      aria-checked={checked}
    >
      <span
        className={`size-4 rounded-full bg-white shadow transition ${
          checked ? "translate-x-2.5" : "-translate-x-2.5"
        }`}
      />
    </Button>
  )
}
export default function SettingsPage() {
  const [tab, setTab] = useState("general")
  const [email, setEmail] = useState(true)
  const [system, setSystem] = useState(true)
  const [payments, setPayments] = useState(true)
  const { theme, setTheme, notify } = useAppStore()
  return (
    <>
      <PageHeader
        title="Settings"
        description="Configure the VocabularyAI platform and your administrator account."
      />
      <div className="grid gap-6 lg:grid-cols-[220px_1fr]">
        <Card className="h-fit p-2">
          {tabs.map(({ id, label, icon: Icon }) => (
            <Button
              key={id}
              variant="ghost"
              className={`w-full justify-start ${
                tab === id ? "bg-primary-soft text-primary" : ""
              }`}
              onClick={() => setTab(id)}
            >
              <Icon className="size-4" />
              {label}
            </Button>
          ))}
        </Card>
        <Card className="p-5 sm:p-7">
          {tab === "general" && (
            <section>
              <div className="text-lg font-bold">Platform settings</div>
              <p className="mt-1 text-sm text-muted-foreground">
                General information and regional preferences.
              </p>
              <div className="mt-6 grid gap-5 sm:grid-cols-2">
                <label className="text-sm font-semibold">
                  Platform Name
                  <Input className="mt-2" defaultValue="VocabularyAI" />
                </label>
                <label className="text-sm font-semibold">
                  Default Language
                  <Select className="mt-2 w-full" defaultValue="Vietnamese">
                    <option>Vietnamese</option>
                    <option>English</option>
                  </Select>
                </label>
                <label className="text-sm font-semibold sm:col-span-2">
                  Platform Description
                  <Textarea
                    className="mt-2"
                    defaultValue="AI-powered English learning platform for Vietnamese learners."
                  />
                </label>
                <label className="text-sm font-semibold">
                  Timezone
                  <Select className="mt-2 w-full" defaultValue="UTC+7">
                    <option>UTC+7</option>
                    <option>UTC+0</option>
                  </Select>
                </label>
              </div>
            </section>
          )}
          {tab === "profile" && (
            <section>
              <div className="text-lg font-bold">Administrator profile</div>
              <p className="mt-1 text-sm text-muted-foreground">
                Update your personal information.
              </p>
              <div className="mt-6 flex items-center gap-4">
                <div className="flex size-16 items-center justify-center rounded-full bg-primary text-lg font-bold text-white">
                  NA
                </div>
                <Button
                  variant="secondary"
                  onClick={() => notify("Avatar picker opened.", "info")}
                >
                  Change avatar
                </Button>
              </div>
              <div className="mt-6 grid gap-5 sm:grid-cols-2">
                <label className="text-sm font-semibold">
                  Full Name
                  <Input className="mt-2" defaultValue="Nguyen Minh Anh" />
                </label>
                <label className="text-sm font-semibold">
                  Email
                  <Input
                    className="mt-2"
                    defaultValue="admin@vocabularyai.vn"
                  />
                </label>
              </div>
            </section>
          )}
          {tab === "security" && (
            <section>
              <div className="text-lg font-bold">Security & sessions</div>
              <p className="mt-1 text-sm text-muted-foreground">
                Change password and review authorization settings.
              </p>
              <div className="mt-6 grid gap-5 sm:grid-cols-2">
                <label className="text-sm font-semibold">
                  Current Password
                  <Input className="mt-2" type="password" />
                </label>
                <div />
                <label className="text-sm font-semibold">
                  New Password
                  <Input className="mt-2" type="password" />
                </label>
                <label className="text-sm font-semibold">
                  Confirm Password
                  <Input className="mt-2" type="password" />
                </label>
              </div>
              <div className="mt-6 rounded-xl bg-muted p-4">
                <div className="flex items-center gap-2 font-semibold">
                  <Shield className="size-4 text-primary" />
                  JWT session policy
                </div>
                <p className="mt-1 text-sm text-muted-foreground">
                  Access token: 15 minutes · Refresh token: 7 days · ADMIN role
                  required.
                </p>
              </div>
            </section>
          )}
          {tab === "notifications" && (
            <section>
              <div className="text-lg font-bold">Notification preferences</div>
              <p className="mt-1 text-sm text-muted-foreground">
                Choose which platform events require your attention.
              </p>
              <div className="mt-5 divide-y divide-border">
                {[
                  ["Email Notifications", email, () => setEmail(!email)],
                  ["System Notifications", system, () => setSystem(!system)],
                  [
                    "Payment Notifications",
                    payments,
                    () => setPayments(!payments),
                  ],
                ].map(([label, value, onChange]) => (
                  <div
                    key={String(label)}
                    className="flex items-center justify-between py-5"
                  >
                    <div>
                      <div className="text-sm font-semibold">
                        {String(label)}
                      </div>
                      <div className="text-xs text-muted-foreground">
                        Receive important {String(label).toLowerCase()}.
                      </div>
                    </div>
                    <Toggle
                      checked={Boolean(value)}
                      onChange={onChange as () => void}
                    />
                  </div>
                ))}
              </div>
            </section>
          )}
          {tab === "appearance" && (
            <section>
              <div className="text-lg font-bold">Appearance</div>
              <p className="mt-1 text-sm text-muted-foreground">
                Select a color mode for the admin console.
              </p>
              <div className="mt-6 grid max-w-xl gap-4 sm:grid-cols-3">
                {[
                  { id: "light", label: "Light", icon: Sun },
                  { id: "dark", label: "Dark", icon: Moon },
                  { id: "system", label: "System", icon: Monitor },
                ].map(({ id, label, icon: Icon }) => (
                  <Button
                    key={id}
                    variant={theme === id ? "primary" : "secondary"}
                    className="h-28 flex-col"
                    onClick={() => setTheme(id as "light" | "dark" | "system")}
                  >
                    <Icon className="size-6" />
                    {label}
                  </Button>
                ))}
              </div>
            </section>
          )}
          <div className="mt-8 flex justify-end border-t border-border pt-5">
            <Button onClick={() => notify("Settings saved successfully.")}>
              Save changes
            </Button>
          </div>
        </Card>
      </div>
    </>
  )
}
