import { BookOpen, GraduationCap, Target } from "lucide-react"
import PageHeader from "../components/common/PageHeader"
import { Badge, Card } from "../components/common/ui"

const levels = [
  ["A1", "Beginner", 620, 1840, 58, 72],
  ["A2", "Elementary", 920, 3240, 66, 76],
  ["B1", "Intermediate", 1240, 4230, 72, 81],
  ["B2", "Upper Intermediate", 1380, 2180, 78, 85],
  ["C1", "Advanced", 860, 960, 84, 89],
]
export default function CefrLevelsPage() {
  return (
    <>
      <PageHeader
        title="CEFR Level Management"
        description="Monitor vocabulary coverage and student performance across proficiency levels."
      />
      <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        {levels.map(([level, name, words, students, completion, score]) => (
          <Card key={level} className="p-6">
            <div className="flex items-start justify-between">
              <div className="flex size-12 items-center justify-center rounded-xl bg-primary text-lg font-extrabold text-white">
                {level}
              </div>
              <Badge tone="success">Active</Badge>
            </div>
            <div className="mt-5 text-xl font-bold">{name}</div>
            <p className="mt-1 text-sm text-muted-foreground">
              Structured {level} curriculum aligned with the Common European
              Framework.
            </p>
            <div className="mt-6 grid grid-cols-2 gap-4">
              <div className="rounded-xl bg-muted p-4">
                <BookOpen className="size-4 text-primary" />
                <div className="mt-2 font-bold">
                  {Number(words).toLocaleString()}
                </div>
                <div className="text-xs text-muted-foreground">
                  Vocabulary words
                </div>
              </div>
              <div className="rounded-xl bg-muted p-4">
                <GraduationCap className="size-4 text-info" />
                <div className="mt-2 font-bold">
                  {Number(students).toLocaleString()}
                </div>
                <div className="text-xs text-muted-foreground">Students</div>
              </div>
            </div>
            <div className="mt-5">
              <div className="flex justify-between text-xs">
                <span className="text-muted-foreground">
                  Average completion
                </span>
                <b>{completion}%</b>
              </div>
              <div className="mt-2 h-2 rounded-full bg-muted">
                <div
                  className="h-full rounded-full bg-primary"
                  style={{ width: `${completion}%` }}
                />
              </div>
            </div>
            <div className="mt-4 flex items-center gap-2 text-sm">
              <Target className="size-4 text-success" />
              <span className="text-muted-foreground">Average quiz score</span>
              <b className="ml-auto">{score}%</b>
            </div>
          </Card>
        ))}
      </div>
    </>
  )
}
