package domain

import domain.Stat.*

import scala.collection.immutable.HashMap
import scala.language.postfixOps

type StatBlock = HashMap[Stat, Int]

case class Hero(baseAttributes: StatBlock = new StatBlock(), job: Option[Job] = None, inventory: Inventory = Inventory()) {
    private val applyJob: StatBlock => StatBlock =
        job.map {
            case Job(modifiers, _) => baseAttributes.merged(modifiers) { case ((k1, v1), (_, v2)) => (k1, v1 + v2) }
        }.getOrElse(_)

    private def stats(): StatBlock = for {
        result <- applyJob(baseAttributes)
    } yield result

    val stat: Stat => Int = stats().getOrElse(_, 0).max(1)
}