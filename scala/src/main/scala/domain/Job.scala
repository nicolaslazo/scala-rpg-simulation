package domain

import domain.stats.Stat.*
import domain.stats.{Stat, StatBlock}

case class Job(modifiers: StatBlock, mainStat: Stat)

object Guerrero extends Job(StatBlock(Health -> 10, Strength -> 15, Intelligence -> -10), Strength)

object Mago extends Job(StatBlock(Strength -> -20, Intelligence -> 20), Intelligence)

object Ladron extends Job(StatBlock(Health -> -5, Speed -> 10), Speed)