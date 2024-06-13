package domain

import domain.Stat.{Intelligence, Speed, Strength}

case class Job(modifiers: StatBlock, mainStat: Stat)

object Guerrero extends Job(StatBlock(health = 10, strength = 15, intelligence = -10), Strength)

object Mago extends Job(StatBlock(strength = -20, intelligence = 20), Intelligence)

object Ladron extends Job(StatBlock(health = -5, speed = 10), Speed)