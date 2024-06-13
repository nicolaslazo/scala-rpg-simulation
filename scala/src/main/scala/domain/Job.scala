package domain

import domain.Stat.*

import scala.collection.immutable.HashMap

case class Job(modifiers: StatBlock, mainStat: Stat)

object Guerrero extends Job(HashMap(Health -> 10, Strength -> 15, Intelligence -> -10), Strength)

object Mago extends Job(HashMap(Strength -> -20, Intelligence -> 20), Intelligence)

object Ladron extends Job(HashMap(Health -> -5, Speed -> 10), Speed)