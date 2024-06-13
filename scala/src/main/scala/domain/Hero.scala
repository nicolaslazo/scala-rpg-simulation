package domain

import domain.Stat.*

case class Hero(baseAttributes: StatBlock = StatBlock(), job: Option[Job] = None) {
    private def stats(): StatBlock = job.map(_.modifiers + baseAttributes).getOrElse(baseAttributes)

    def stat(which: Stat): Int =
        1.max(
            which match
                case Health => stats().health
                case Strength => stats().strength
                case Speed => stats().speed
                case Intelligence => stats().intelligence
        )
}