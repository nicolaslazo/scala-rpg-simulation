package domain.missions

import domain.*
import domain.Stat.*
import domain.equipment.Item
import domain.equipment.ItemSlot.Neck

import scala.util.{Failure, Success, Try}

case class Task(effect: Hero => Hero, difficultyRating: (Hero, Team) => Try[Int], reward: Team => Team)

object PelearContraMonstruo extends Task(
    effect = hero => if hero.stat(Strength) < 20 then
        hero.applyModifiersOnBaseAttributes(StatBlock(Health -> -1)) else hero,
    difficultyRating = (_, team) => if team.leader.map(_.job).contains(Guerrero) then Success(20) else Success(10),
    reward = _.getGold(100))

object ForzarPuerta extends Task(
    effect = hero =>
        if Set(Mago, Guerrero).contains(hero.job.getOrElse(None))
        then hero
        else hero.applyModifiersOnBaseAttributes(StatBlock(Health -> -5, Strength -> 1)),
    difficultyRating = (hero, team) => Success(hero.stat(Intelligence) + team.members.count(_.job.contains(Ladron))),
    reward = _.addMember(Hero()))

object RobarTalisman extends Task(
    effect = _.equip(Item(slot = Neck), Neck).get,
    difficultyRating = (hero, team) =>
        if team.leader.flatMap(_.job).contains(Ladron)
        then Success(hero.stat(Speed))
        else Failure(TaskFailedException("No se puede robar un talisman si el líder del equipo no es un ladrón")),
    reward = _.getGold(1000))